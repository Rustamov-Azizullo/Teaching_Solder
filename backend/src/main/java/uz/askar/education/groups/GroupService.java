package uz.askar.education.groups;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.common.ForbiddenException;
import uz.askar.education.groups.GroupDtos.LeaderDto;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.groups.GroupDtos.GroupDto;
import uz.askar.education.groups.GroupDtos.GroupRequest;
import uz.askar.education.groups.GroupDtos.GroupSummary;
import uz.askar.education.groups.GroupDtos.MemberDto;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.OrganizationDtos.NamedRef;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierRepository;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final int MAX_VOCATIONAL_MONTHS = 6;

    private final StudyGroupRepository groups;
    private final MilitaryUnitRepository militaryUnits;
    private final EducationInstitutionRepository institutions;
    private final DictionaryItemRepository dictionaryItems;
    private final SoldierRepository soldiers;
    private final TeacherRepository teachers;
    private final TeacherService teacherService;
    private final uz.askar.education.cycles.CycleService cycleService;
    private final uz.askar.education.results.CourseResultRepository courseResults;
    private final GroupLeaderService groupLeaderService;
    private final PermissionEvaluatorService permissions;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<GroupSummary> list(GroupType type) {
        AccessScope scope = currentUser.scope();
        return groups.search(type, scope.districtFilter(), scope.unitFilter()).stream()
                .map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public GroupDto get(Long id) {
        return toDto(findInScope(id));
    }

    @Transactional
    public GroupDto create(GroupRequest request) {
        cycleService.requireCurrentOpen();
        StudyGroup group = new StudyGroup();
        apply(group, request);
        StudyGroup saved = groups.save(group);
        applyLeader(saved, request);
        audit.record("CREATE", "StudyGroup", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Transactional
    public GroupDto update(Long id, GroupRequest request) {
        StudyGroup group = findInScope(id);
        requireEditable(group);
        requireMembershipCompatible(group, request);
        apply(group, request);
        applyLeader(group, request);
        audit.record("UPDATE", "StudyGroup", id, group.getName());
        return toDto(group);
    }

    /** Guruhni o'chiradi; darslar va natijalar u bilan birga o'chadi, shuning uchun tasdiqlangan kurs yakuni bo'lsa ruxsat berilmaydi. */
    @Transactional
    public void delete(Long id) {
        StudyGroup group = findInScope(id);
        cycleService.requireOpen(group.getCycleYear());
        if (group.getCourseApprovedAt() != null) {
            throw new BusinessRuleException("Kurs yakuni tasdiqlangan guruhni o'chirib bo'lmaydi");
        }
        String name = group.getName();
        try {
            groups.delete(group);
            groups.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Guruh boshqa ma'lumotlarga bog'langan, o'chirib bo'lmaydi");
        }
        audit.record("DELETE", "StudyGroup", id, name);
    }

    @Transactional
    public GroupDto removeLeader(Long id) {
        StudyGroup group = findInScope(id);
        groupLeaderService.detach(group);
        audit.record("REMOVE_LEADER", "StudyGroup", id, "guruh kattasi olib tashlandi");
        return toDto(group);
    }

    /** Shu guruhdan tashqari, o'sha harbiy qismning boshqa guruhlariga biriktirilgan askarlar id lari. */
    @Transactional(readOnly = true)
    public List<Long> soldierIdsInOtherGroups(Long id) {
        StudyGroup group = findInScope(id);
        return groups.findSoldierIdsInOtherGroups(group.getMilitaryUnit().getId(), group.getId());
    }

    @Transactional
    public GroupDto replaceMembers(Long id, Set<Long> soldierIds) {
        StudyGroup group = findInScope(id);
        requireEditable(group);
        Set<Long> ids = soldierIds == null ? Set.of() : soldierIds;
        List<Soldier> found = soldiers.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new NotFoundException("Ba'zi askarlar topilmadi");
        }
        for (Soldier soldier : found) {
            if (!soldier.getMilitaryUnit().getId().equals(group.getMilitaryUnit().getId())) {
                throw new BusinessRuleException(soldier.getFullName() + " boshqa qismga tegishli");
            }
            if (groups.countOtherMemberships(soldier.getId(), group.getId()) > 0) {
                throw new BusinessRuleException(soldier.getFullName() + " boshqa guruhda allaqachon bor");
            }
        }
        removeResultsOfDroppedMembers(group, found);
        group.getSoldiers().clear();
        group.getSoldiers().addAll(found);
        audit.record("UPDATE_MEMBERS", "StudyGroup", id, "askarlar soni: " + found.size());
        return toDto(group);
    }

    @Transactional
    public GroupDto replaceTeachers(Long id, Set<Long> teacherIds) {
        StudyGroup group = findInScope(id);
        requireEditable(group);
        Set<Long> ids = teacherIds == null ? Set.of() : teacherIds;
        List<Teacher> found = teachers.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new NotFoundException("Ba'zi o'qituvchilar topilmadi");
        }
        found.forEach(teacher -> {
            boolean hasContract = teacher.getInstitution().getContractedUnits().stream()
                    .anyMatch(unit -> unit.getId().equals(group.getMilitaryUnit().getId()));
            if (!hasContract) {
                throw new BusinessRuleException(teacher.getFullName() + " muassasasi bu harbiy qismga biriktirilmagan");
            }
        });
        group.getTeachers().clear();
        group.getTeachers().addAll(found);
        audit.record("UPDATE_TEACHERS", "StudyGroup", id, "o'qituvchilar soni: " + found.size());
        return toDto(group);
    }

    /** Sikli yopilgan yoki kurs yakuni tasdiqlangan guruhning tarkibi va sozlamalari o'zgartirilmaydi. */
    private void requireEditable(StudyGroup group) {
        cycleService.requireOpen(group.getCycleYear());
        if (group.getCourseApprovedAt() != null) {
            throw new BusinessRuleException("Kurs yakuni tasdiqlangan guruhni o'zgartirib bo'lmaydi");
        }
    }

    /** Harbiy qism yoki tur o'zgarsa, mavjud askarlar va o'qituvchilar yangi sozlamaga mos kelmay qoladi. */
    private void requireMembershipCompatible(StudyGroup group, GroupRequest request) {
        boolean unitChanged = !group.getMilitaryUnit().getId().equals(request.militaryUnitId());
        boolean typeChanged = group.getType() != request.type();
        boolean hasMembers = !group.getSoldiers().isEmpty() || !group.getTeachers().isEmpty();
        if ((unitChanged || typeChanged) && hasMembers) {
            throw new BusinessRuleException("Harbiy qism yoki guruh turini o'zgartirishdan oldin askarlar va "
                    + "o'qituvchilarni guruhdan chiqaring");
        }
    }

    /** Guruhdan chiqarilgan askarlarning kurs natijalari qolib ketmasligi uchun o'chiriladi. */
    private void removeResultsOfDroppedMembers(StudyGroup group, List<Soldier> newMembers) {
        Set<Long> keptIds = newMembers.stream().map(Soldier::getId).collect(java.util.stream.Collectors.toSet());
        Set<Long> droppedIds = group.getSoldiers().stream().map(Soldier::getId)
                .filter(soldierId -> !keptIds.contains(soldierId)).collect(java.util.stream.Collectors.toSet());
        if (!droppedIds.isEmpty()) {
            courseResults.deleteByGroupIdAndSoldierIdIn(group.getId(), droppedIds);
        }
    }

    /** Guruhga kirish huquqi: vakolat doirasi. */
    public StudyGroup findInScope(Long id) {
        StudyGroup group = groups.findById(id).orElseThrow(() -> new NotFoundException("Guruh topilmadi"));
        currentUser.scope().require(group.getMilitaryUnit().getMilitaryDistrict().getId(),
                group.getMilitaryUnit().getId());
        return group;
    }

    /** Guruh kattasi so'rovda berilgan bo'lsagina o'zgaradi; buning uchun alohida ruxsat kerak. */
    private void applyLeader(StudyGroup group, GroupRequest request) {
        if (request.leader() == null) {
            return;
        }
        if (!permissions.currentUserHas(Permission.GROUP_LEADER_ASSIGN)) {
            throw new ForbiddenException("Guruh kattasini kiritish uchun vakolatingiz yetarli emas");
        }
        groupLeaderService.assign(group, request.leader());
    }

    private void apply(StudyGroup group, GroupRequest request) {
        MilitaryUnit unit = militaryUnits.findById(request.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        validateDates(request);
        group.setCycleYear(request.startDate().getYear());
        group.setName(request.name());
        group.setType(request.type());
        group.setMilitaryUnit(unit);
        group.setStartDate(request.startDate());
        group.setEndDate(request.endDate());
        group.setClassroom(request.classroom());
        group.setInstitution(contractedInstitution(request.institutionId(), unit));
        applyCurriculum(group, request);
    }

    /** Muassasa ixtiyoriy, lekin tanlansa guruh harbiy qismi bilan shartnomasi (biriktirilishi) bo'lishi shart. */
    private EducationInstitution contractedInstitution(Long institutionId, MilitaryUnit unit) {
        if (institutionId == null) {
            return null;
        }
        EducationInstitution institution = institutions.findById(institutionId)
                .orElseThrow(() -> new NotFoundException("Ta'lim muassasasi topilmadi"));
        boolean hasContract = institution.getContractedUnits().stream().anyMatch(item -> item.getId().equals(unit.getId()));
        if (!hasContract) {
            throw new BusinessRuleException("Tanlangan harbiy qism bilan bu muassasaning shartnomasi yo'q");
        }
        return institution;
    }

    private void applyCurriculum(StudyGroup group, GroupRequest request) {
        if (request.type() == GroupType.VOCATIONAL) {
            if (request.professionId() == null) {
                throw new BusinessRuleException("Kasb kursi uchun kasb tanlanishi shart");
            }
            group.setProfession(dictionaryItem(request.professionId(), DictionaryType.PROFESSION));
            group.getSubjects().clear();
            return;
        }
        Set<Long> subjectIds = request.subjectIds() == null ? Set.of() : request.subjectIds();
        if (subjectIds.isEmpty()) {
            throw new BusinessRuleException("OTM tayyorlov kursi uchun kamida bitta fan tanlanishi shart");
        }
        group.setProfession(null);
        group.getSubjects().clear();
        subjectIds.forEach(subjectId -> group.getSubjects().add(dictionaryItem(subjectId, DictionaryType.SUBJECT)));
    }

    private void validateDates(GroupRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessRuleException("Tugash sanasi boshlanish sanasidan oldin bo'lishi mumkin emas");
        }
        LocalDate latestEnd = request.startDate().plusMonths(MAX_VOCATIONAL_MONTHS);
        if (request.type() == GroupType.VOCATIONAL && request.endDate().isAfter(latestEnd)) {
            throw new BusinessRuleException("Kasb kursi 6 oydan oshmasligi kerak");
        }
    }

    private DictionaryItem dictionaryItem(Long id, DictionaryType type) {
        return dictionaryItems.findById(id).filter(item -> item.getType() == type)
                .orElseThrow(() -> new NotFoundException("Ma'lumotnoma yozuvi topilmadi: " + type));
    }

    private GroupSummary toSummary(StudyGroup g) {
        return new GroupSummary(g.getId(), g.getName(), g.getType(), g.getMilitaryUnit().getId(),
                g.getMilitaryUnit().getName(), g.getProfession() != null ? g.getProfession().getName() : null,
                g.getSubjects().stream().map(DictionaryItem::getName).sorted().toList(), g.getStartDate(),
                g.getEndDate(), g.getSoldiers().size(), g.getLeader() != null ? g.getLeader().getFullName() : null);
    }

    private GroupDto toDto(StudyGroup g) {
        boolean canSeePinfl = permissions.currentUserHas(Permission.SOLDIER_READ);
        return new GroupDto(g.getId(), g.getName(), g.getType(), g.getMilitaryUnit().getId(),
                g.getMilitaryUnit().getName(),
                g.getInstitution() == null ? null : new NamedRef(g.getInstitution().getId(), g.getInstitution().getName()),
                g.getProfession() == null ? null : new NamedRef(g.getProfession().getId(), g.getProfession().getName()),
                refs(g.getSubjects()), g.getStartDate(), g.getEndDate(), g.getClassroom(),
                g.getLeader() == null ? null : LeaderDto.from(g.getLeader()),
                g.getSoldiers().stream().sorted(Comparator.comparing(Soldier::getFullName))
                        .map(s -> new MemberDto(s.getId(), canSeePinfl ? s.getPinfl() : null, s.getFullName())).toList(),
                g.getTeachers().stream().sorted(Comparator.comparing(Teacher::getFullName))
                        .map(teacherService::toDto).toList());
    }

    private List<NamedRef> refs(Collection<DictionaryItem> items) {
        return new HashSet<>(items).stream().sorted(Comparator.comparing(DictionaryItem::getName))
                .map(item -> new NamedRef(item.getId(), item.getName())).toList();
    }
}
