package uz.askar.education.groups;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.groups.GroupDtos.InstitutionDto;
import uz.askar.education.groups.GroupDtos.InstitutionRequest;
import uz.askar.education.groups.GroupDtos.TeacherDto;
import uz.askar.education.groups.GroupDtos.TeacherRequest;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private static final int ACCESS_WARNING_DAYS = 30;

    private final TeacherRepository teachers;
    private final EducationInstitutionRepository institutions;
    private final MilitaryUnitRepository militaryUnits;
    private final RegionRepository regions;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<InstitutionDto> listInstitutions() {
        return institutions.findAllByOrderByNameAsc().stream().map(InstitutionDto::from).toList();
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
        EducationInstitution saved = institutions.save(institution);
        audit.record("CREATE", "EducationInstitution", saved.getId(), saved.getName());
        return InstitutionDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TeacherDto> list() {
        AccessScope scope = currentUser.scope();
        return teachers.findInScope(scope.districtFilter(), scope.unitFilter()).stream().map(this::toDto).toList();
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
        Teacher teacher = teachers.findById(id).orElseThrow(() -> new NotFoundException("O'qituvchi topilmadi"));
        currentUser.scope().require(teacher.getMilitaryUnit().getMilitaryDistrict().getId(),
                teacher.getMilitaryUnit().getId());
        apply(teacher, request);
        audit.record("UPDATE", "Teacher", id, teacher.getFullName());
        return toDto(teacher);
    }

    Teacher findInScope(Long id) {
        Teacher teacher = teachers.findById(id).orElseThrow(() -> new NotFoundException("O'qituvchi topilmadi"));
        currentUser.scope().require(teacher.getMilitaryUnit().getMilitaryDistrict().getId(),
                teacher.getMilitaryUnit().getId());
        return teacher;
    }

    TeacherDto toDto(Teacher t) {
        boolean expiring = t.getAccessValidUntil() != null
                && t.getAccessValidUntil().isBefore(LocalDate.now().plusDays(ACCESS_WARNING_DAYS));
        return new TeacherDto(t.getId(), t.getFullName(), t.getSpecialty(), t.getInstitution().getId(),
                t.getInstitution().getName(), t.getMilitaryUnit().getId(), t.getAccessOrderNo(),
                t.getAccessValidUntil(), expiring);
    }

    private void apply(Teacher teacher, TeacherRequest request) {
        MilitaryUnit unit = militaryUnits.findById(request.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        teacher.setFullName(request.fullName());
        teacher.setSpecialty(request.specialty());
        teacher.setInstitution(institutions.findById(request.institutionId())
                .orElseThrow(() -> new NotFoundException("Ta'lim muassasasi topilmadi")));
        teacher.setMilitaryUnit(unit);
        teacher.setAccessOrderNo(request.accessOrderNo());
        teacher.setAccessValidUntil(request.accessValidUntil());
    }
}
