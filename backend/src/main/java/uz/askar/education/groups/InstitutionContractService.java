package uz.askar.education.groups;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.groups.GroupDtos.ContractDto;
import uz.askar.education.groups.GroupDtos.ContractRequest;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;

/** Muassasani harbiy qismga biriktirish (shartnoma): o'qituvchi faqat shartnomali muassasadan qo'shiladi. */
@Service
@RequiredArgsConstructor
public class InstitutionContractService {

    private final EducationInstitutionRepository institutions;
    private final MilitaryUnitRepository militaryUnits;
    private final StudyGroupRepository groups;
    private final CurrentUser currentUser;
    private final AuditService audit;

    /** Vakolat doirasidagi harbiy qismlarning shartnomalari: qism nomi, keyin muassasa nomi bo'yicha. */
    @Transactional(readOnly = true)
    public List<ContractDto> list() {
        AccessScope scope = currentUser.scope();
        return institutions.findAllByOrderByNameAsc().stream()
                .flatMap(institution -> institution.getContractedUnits().stream()
                        .filter(unit -> scope.covers(unit.getMilitaryDistrict().getId(), unit.getId()))
                        .map(unit -> toDto(institution, unit)))
                .sorted(Comparator.comparing(ContractDto::unitName).thenComparing(ContractDto::institutionName))
                .toList();
    }

    @Transactional
    public ContractDto create(ContractRequest request) {
        EducationInstitution institution = findInstitution(request.institutionId());
        MilitaryUnit unit = findUnitInScope(request.unitId());
        if (!institution.getContractedUnits().add(unit)) {
            throw new BusinessRuleException("Bu muassasa shu harbiy qismga allaqachon biriktirilgan");
        }
        audit.record("CREATE", "InstitutionContract", institution.getId(), institution.getName() + " -> " + unit.getName());
        return toDto(institution, unit);
    }

    /** Biriktirishni o'zgartiradi (muassasa va/yoki qism); eski juftlikda o'qituvchi yoki guruh bo'lsa — o'zgartirilmaydi. */
    @Transactional
    public ContractDto update(Long institutionId, Long unitId, ContractRequest request) {
        if (institutionId.equals(request.institutionId()) && unitId.equals(request.unitId())) {
            return toDto(findInstitution(institutionId), findUnitInScope(unitId));
        }
        EducationInstitution target = findInstitution(request.institutionId());
        MilitaryUnit targetUnit = findUnitInScope(request.unitId());
        if (target.getContractedUnits().contains(targetUnit)) {
            throw new BusinessRuleException("Bu muassasa shu harbiy qismga allaqachon biriktirilgan");
        }
        delete(institutionId, unitId);
        target.getContractedUnits().add(targetUnit);
        audit.record("UPDATE", "InstitutionContract", target.getId(), target.getName() + " -> " + targetUnit.getName());
        return toDto(target, targetUnit);
    }

    /** Shartnomani bekor qiladi; qismda shu muassasadan o'qituvchi yoki guruh bo'lsa — bekor qilinmaydi. */
    @Transactional
    public void delete(Long institutionId, Long unitId) {
        EducationInstitution institution = findInstitution(institutionId);
        MilitaryUnit unit = findUnitInScope(unitId);
        if (groups.existsByInstitutionIdAndMilitaryUnitId(institutionId, unitId)
                || groups.existsByUnitAndTeacherInstitution(unitId, institutionId)) {
            throw new BusinessRuleException("Bu qismda shu muassasaning guruhlari yoki o'qituvchilari biriktirilgan, "
                    + "biriktirishni bekor qilib bo'lmaydi");
        }
        if (!institution.getContractedUnits().remove(unit)) {
            throw new NotFoundException("Biriktirish topilmadi");
        }
        audit.record("DELETE", "InstitutionContract", institutionId, institution.getName() + " -> " + unit.getName());
    }

    private EducationInstitution findInstitution(Long id) {
        return institutions.findById(id).orElseThrow(() -> new NotFoundException("Muassasa topilmadi"));
    }

    private MilitaryUnit findUnitInScope(Long id) {
        MilitaryUnit unit = militaryUnits.findById(id).orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        return unit;
    }

    private static ContractDto toDto(EducationInstitution institution, MilitaryUnit unit) {
        return new ContractDto(institution.getId(), institution.getName(), institution.getType(), unit.getId(),
                unit.getName(), unit.getMilitaryDistrict().getName());
    }
}
