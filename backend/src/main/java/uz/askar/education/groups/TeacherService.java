package uz.askar.education.groups;

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
import uz.askar.education.groups.GroupDtos.InstitutionDto;
import uz.askar.education.groups.GroupDtos.InstitutionRequest;
import uz.askar.education.groups.GroupDtos.TeacherDto;
import uz.askar.education.groups.GroupDtos.TeacherRequest;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teachers;
    private final EducationInstitutionRepository institutions;
    private final DictionaryItemRepository dictionaryItems;
    private final RegionRepository regions;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<InstitutionDto> listInstitutions(Long unitId) {
        return institutions.findAllByOrderByNameAsc().stream()
                .filter(institution -> unitId == null || hasContract(institution, unitId))
                .map(InstitutionDto::from).toList();
    }

    @Transactional
    public InstitutionDto createInstitution(InstitutionRequest request) {
        EducationInstitution institution = new EducationInstitution();
        institution.setType(request.type());
        institution.setName(request.name());
        if (request.regionId() != null) {
            institution.setRegion(regions.findById(request.regionId())
                    .orElseThrow(() -> new NotFoundException("Viloyat topilmadi")));
        }
        replaceSpecialties(institution, request.professionIds(), request.subjectIds());
        EducationInstitution saved = institutions.save(institution);
        audit.record("CREATE", "EducationInstitution", saved.getId(), saved.getName());
        return InstitutionDto.from(saved);
    }

    @Transactional
    public InstitutionDto updateInstitution(Long id, InstitutionRequest request) {
        EducationInstitution institution = findInstitution(id);
        institution.setType(request.type());
        institution.setName(request.name());
        replaceSpecialties(institution, request.professionIds(), request.subjectIds());
        audit.record("UPDATE", "EducationInstitution", id, institution.getName());
        return InstitutionDto.from(institution);
    }

    /** Muassasani o'chiradi; o'qituvchi yoki guruh unga bog'langan bo'lsa — o'chirilmaydi. */
    @Transactional
    public void deleteInstitution(Long id) {
        EducationInstitution institution = findInstitution(id);
        String name = institution.getName();
        try {
            institutions.delete(institution);
            institutions.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Muassasaga o'qituvchilar yoki guruhlar biriktirilgan, o'chirib bo'lmaydi");
        }
        audit.record("DELETE", "EducationInstitution", id, name);
    }

    private static boolean hasContract(EducationInstitution institution, Long unitId) {
        return institution.getContractedUnits().stream().anyMatch(unit -> unit.getId().equals(unitId));
    }

    /** Muassasada o'qitiladigan kasb va fanlarni almashtiradi ({@code null} — o'zgartirmaslik). */
    private void replaceSpecialties(EducationInstitution institution, Set<Long> professionIds, Set<Long> subjectIds) {
        if (professionIds == null && subjectIds == null) {
            return;
        }
        Set<DictionaryItem> result = new HashSet<>();
        result.addAll(itemsOfType(professionIds, DictionaryType.PROFESSION, institution));
        result.addAll(itemsOfType(subjectIds, DictionaryType.SUBJECT, institution));
        institution.setSpecialties(result);
    }

    /** {@code ids == null} bo'lsa, shu turdagi mavjud yozuvlar saqlanadi. */
    private List<DictionaryItem> itemsOfType(Set<Long> ids, DictionaryType type, EducationInstitution institution) {
        if (ids == null) {
            return institution.getSpecialties().stream().filter(item -> item.getType() == type).toList();
        }
        List<DictionaryItem> items = dictionaryItems.findAllById(ids);
        if (items.size() != ids.size() || items.stream().anyMatch(item -> item.getType() != type)) {
            throw new BusinessRuleException(type == DictionaryType.PROFESSION
                    ? "Kasblar ro'yxatidan mavjud kasblarni tanlang" : "Fanlar ro'yxatidan mavjud fanlarni tanlang");
        }
        return items;
    }

    private EducationInstitution findInstitution(Long id) {
        return institutions.findById(id).orElseThrow(() -> new NotFoundException("Muassasa topilmadi"));
    }

    @Transactional(readOnly = true)
    public List<TeacherDto> list() {
        return teachers.findAllByOrderByFullNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public TeacherDto create(TeacherRequest request) {
        Teacher teacher = new Teacher();
        apply(teacher, request);
        Teacher saved = teachers.save(teacher);
        audit.record("CREATE", "Teacher", saved.getId(), saved.getFullName());
        return toDto(saved);
    }

    @Transactional
    public TeacherDto update(Long id, TeacherRequest request) {
        Teacher teacher = find(id);
        apply(teacher, request);
        audit.record("UPDATE", "Teacher", id, teacher.getFullName());
        return toDto(teacher);
    }

    /** O'qituvchini o'chiradi va uni barcha guruhlardan ajratadi. */
    @Transactional
    public void delete(Long id) {
        Teacher teacher = find(id);
        String name = teacher.getFullName();
        teachers.detachFromGroups(id);
        teachers.delete(teacher);
        audit.record("DELETE", "Teacher", id, name);
    }

    private Teacher find(Long id) {
        return teachers.findById(id).orElseThrow(() -> new NotFoundException("O'qituvchi topilmadi"));
    }

    TeacherDto toDto(Teacher t) {
        return new TeacherDto(t.getId(), t.getFullName(), t.getSpecialty(),
                t.getSpecialties().stream().map(DictionaryItem::getId).sorted().toList(), t.getInstitution().getId(),
                t.getInstitution().getName());
    }

    /** Mutaxassisliklar faqat faol kasb yoki fanlar bo'lishi mumkin; nomlar tartiblangan holda vergul bilan birlashtiriladi. */
    private List<DictionaryItem> specialtiesOf(Set<Long> ids) {
        List<DictionaryItem> items = dictionaryItems.findAllById(ids);
        boolean allValid = items.size() == ids.size() && items.stream().allMatch(item -> item.isActive()
                && (item.getType() == DictionaryType.PROFESSION || item.getType() == DictionaryType.SUBJECT));
        if (!allValid) {
            throw new BusinessRuleException("Mutaxassisliklar kasblar yoki fanlar ro'yxatidan tanlanishi kerak");
        }
        return items.stream().sorted(java.util.Comparator.comparing(DictionaryItem::getName)).toList();
    }

    private void apply(Teacher teacher, TeacherRequest request) {
        teacher.setFullName(request.fullName());
        List<DictionaryItem> specialties = specialtiesOf(request.specialtyIds());
        teacher.setSpecialties(new HashSet<>(specialties));
        teacher.setSpecialty(specialties.stream().map(DictionaryItem::getName).collect(java.util.stream.Collectors.joining(", ")));
        teacher.setInstitution(institutions.findById(request.institutionId())
                .orElseThrow(() -> new NotFoundException("Ta'lim muassasasi topilmadi")));
    }
}
