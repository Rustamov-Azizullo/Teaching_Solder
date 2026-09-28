package uz.askar.education.groups;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.ForbiddenException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.groups.GroupDtos.GroupDto;
import uz.askar.education.groups.GroupDtos.GroupLeaderOption;
import uz.askar.education.groups.GroupDtos.GroupRequest;
import uz.askar.education.groups.GroupDtos.GroupSummary;
import uz.askar.education.groups.GroupDtos.LeaderRequest;
import uz.askar.education.groups.GroupDtos.MemberDto;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.OrganizationDtos.NamedRef;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.security.Role;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierRepository;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;

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
    private final AppUserRepository users;
    private final CurrentUser currentUser;
    private final PermissionEvaluatorService permissions;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<GroupSummary> list(GroupType type) {
        AccessScope scope = currentUser.scope();
        return groups.search(type, scope.districtFilter(), scope.unitFilter(), leaderRestriction()).stream()
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
        group.setCycleYear(request.startDate().getYear());
        apply(group, request);
        StudyGroup saved = groups.save(group);
        audit.record("CREATE", "StudyGroup", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Transactional
    public GroupDto update(Long id, GroupRequest request) {
        StudyGroup group = findInScope(id);
        apply(group, request);
        audit.record("UPDATE", "StudyGroup", id, group.getName());
        return toDto(group);
    }

    @Transactional
    public GroupDto assignLeader(Long id, LeaderRequest request) {
        StudyGroup group = findInScope(id);
        AppUser leader = users.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));
        boolean sameUnit = group.getMilitaryUnit().getId().equals(leader.effectiveUnitId());
        if (leader.getRole() != Role.USER || !leader.isActive() || !sameUnit) {
            throw new BusinessRuleException("Guruh kattasi shu qismga biriktirilgan faol foydalanuvchi bo'lishi kerak");
        }
        group.setLeader(leader);
        group.setLeaderOrderNo(request.orderNo());
        group.setLeaderOrderDate(request.orderDate());
        audit.record("ASSIGN_LEADER", "StudyGroup", id, "guruh kattasi=" + leader.getFullName()
                + ", buyruq=" + request.orderNo());
        return toDto(group);
    }

    @Transactional
    public GroupDto replaceMembers(Long id, Set<Long> soldierIds) {
        StudyGroup group = findInScope(id);
        Set<Long> ids = soldierIds == null ? Set.of() : soldierIds;
        List<Soldier> found = soldiers.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new NotFoundException("Ba'zi askarlar topilmadi");
        }
        for (Soldier soldier : found) {
            if (!soldier.getMilitaryUnit().getId().equals(group.getMilitaryUnit().getId())) {
                throw new BusinessRuleException(soldier.getFullName() + " boshqa qismga tegishli");
            }
            if (groups.countOtherMemberships(soldier.getId(), group.getType(), group.getCycleYear(),
                    group.getId()) > 0) {
                throw new BusinessRuleException(soldier.getFullName() + " shu turdagi boshqa guruhda allaqachon bor");
            }
        }
        group.getSoldiers().clear();
        group.getSoldiers().addAll(found);
        audit.record("UPDATE_MEMBERS", "StudyGroup", id, "askarlar soni: " + found.size());
        return toDto(group);
    }

    @Transactional
    public GroupDto replaceTeachers(Long id, Set<Long> teacherIds) {
        StudyGroup group = findInScope(id);
        Set<Long> ids = teacherIds == null ? Set.of() : teacherIds;
        List<Teacher> found = teachers.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new NotFoundException("Ba'zi o'qituvchilar topilmadi");
        }
        found.forEach(teacher -> {
            if (!teacher.getMilitaryUnit().getId().equals(group.getMilitaryUnit().getId())) {
                throw new BusinessRuleException(teacher.getFullName() + " boshqa qismga biriktirilgan");
            }
        });
        group.getTeachers().clear();
        group.getTeachers().addAll(found);
        audit.record("UPDATE_TEACHERS", "StudyGroup", id, "o'qituvchilar soni: " + found.size());
        return toDto(group);
    }

    @Transactional(readOnly = true)
    public List<GroupLeaderOption> leaderOptions(Long unitId) {
        MilitaryUnit unit = militaryUnits.findById(unitId)
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        return users.findByRoleAndLocationMilitaryUnitIdAndActiveTrue(Role.USER, unitId).stream()
                .map(user -> new GroupLeaderOption(user.getId(), user.getFullName())).toList();
    }

    /** Guruhga kirish huquqi: vakolat doirasi, guruh kattasi uchun esa faqat o'z guruhi. */
    public StudyGroup findInScope(Long id) {
        StudyGroup group = groups.findById(id).orElseThrow(() -> new NotFoundException("Guruh topilmadi"));
        currentUser.scope().require(group.getMilitaryUnit().getMilitaryDistrict().getId(),
                group.getMilitaryUnit().getId());
        Long leaderId = leaderRestriction();
        if (leaderId != null && (group.getLeader() == null || !group.getLeader().getId().equals(leaderId))) {
            throw new ForbiddenException("Bu guruh sizga biriktirilmagan");
        }
        return group;
    }

    /**
     * Guruh kattasi cheklovi (rolga emas, ma'lumotga asoslangan): joriy foydalanuvchi kamida bitta guruhning
     * kattasi bo'lsa va guruhlarni boshqarish ({@code GROUP_WRITE}) ruxsatiga ega bo'lmasa — faqat o'z
     * guruh(lar)ini ko'radi. Qaytaradi: cheklov uchun foydalanuvchi identifikatori yoki {@code null} (cheklovsiz).
     */
    public Long leaderRestriction() {
        Long userId = currentUser.id();
        if (!groups.existsByLeaderId(userId) || permissions.currentUserHas(Permission.GROUP_WRITE)) {
            return null;
        }
        return userId;
    }

    private void apply(StudyGroup group, GroupRequest request) {
        MilitaryUnit unit = militaryUnits.findById(request.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        validateDates(request);
        group.setName(request.name());
        group.setType(request.type());
        group.setMilitaryUnit(unit);
        group.setStartDate(request.startDate());
        group.setEndDate(request.endDate());
        group.setClassroom(request.classroom());
        group.setInstitution(request.institutionId() == null ? null : institutions.findById(request.institutionId())
                .orElseThrow(() -> new NotFoundException("Ta'lim muassasasi topilmadi")));
        applyCurriculum(group, request);
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
        return new GroupDto(g.getId(), g.getName(), g.getType(), g.getMilitaryUnit().getId(),
                g.getMilitaryUnit().getName(),
                g.getInstitution() == null ? null : new NamedRef(g.getInstitution().getId(), g.getInstitution().getName()),
                g.getProfession() == null ? null : new NamedRef(g.getProfession().getId(), g.getProfession().getName()),
                refs(g.getSubjects()), g.getStartDate(), g.getEndDate(), g.getClassroom(),
                g.getLeader() == null ? null : new NamedRef(g.getLeader().getId(), g.getLeader().getFullName()),
                g.getLeaderOrderNo(), g.getLeaderOrderDate(),
                g.getSoldiers().stream().sorted(Comparator.comparing(Soldier::getFullName))
                        .map(s -> new MemberDto(s.getId(), s.getPinfl(), s.getFullName())).toList(),
                g.getTeachers().stream().sorted(Comparator.comparing(Teacher::getFullName))
                        .map(teacherService::toDto).toList());
    }

    private List<NamedRef> refs(Collection<DictionaryItem> items) {
        return new HashSet<>(items).stream().sorted(Comparator.comparing(DictionaryItem::getName))
                .map(item -> new NamedRef(item.getId(), item.getName())).toList();
    }
}
