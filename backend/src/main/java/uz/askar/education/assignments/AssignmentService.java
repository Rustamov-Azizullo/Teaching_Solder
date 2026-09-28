package uz.askar.education.assignments;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.groups.EducationInstitutionRepository;
import uz.askar.education.groups.GroupType;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Permission;
import uz.askar.education.security.Role;

/**
 * Biriktirish oqimi: taklif → ko'rib chiqilmoqda → tasdiqlangan / rad etilgan.
 * Qism/okrug taklif kiritadi, respublika darajasi (ASSIGNMENT_DECIDE ruxsati) qaror chiqaradi,
 * shartnoma va qo'shma reja tasdiqdan keyin to'ldiriladi.
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    public record ProposalRequest(@NotNull Long militaryUnitId, @NotNull Long institutionId,
                                  @NotNull GroupType direction, @Size(max = 500) String note) {
    }

    public record DecisionRequest(@NotNull Boolean approve, @Size(max = 255) String basisDocument,
                                  LocalDate validFrom, LocalDate validTo, @Size(max = 500) String note) {
    }

    public record ContractRequest(@Size(max = 60) String contractNo, LocalDate contractDate,
                                  @Size(max = 500) String jointPlan) {
    }

    public record AssignmentDto(Long id, Long militaryUnitId, String militaryUnitName, Long institutionId,
                                String institutionName, GroupType direction, AssignmentStatus status,
                                String basisDocument, LocalDate validFrom, LocalDate validTo, String contractNo,
                                LocalDate contractDate, String jointPlan, String proposalNote, String decisionNote,
                                String proposedBy, String decidedBy, LocalDateTime createdAt) {
    }

    private final AssignmentRepository assignments;
    private final MilitaryUnitRepository militaryUnits;
    private final EducationInstitutionRepository institutions;
    private final NotificationService notifications;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<AssignmentDto> list() {
        var scope = currentUser.scope();
        return assignments.findInScope(scope.districtFilter(), scope.unitFilter()).stream().map(this::toDto).toList();
    }

    @Transactional
    public AssignmentDto propose(ProposalRequest request) {
        var unit = militaryUnits.findById(request.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        Assignment assignment = new Assignment();
        assignment.setMilitaryUnit(unit);
        assignment.setInstitution(institutions.findById(request.institutionId())
                .orElseThrow(() -> new NotFoundException("Ta'lim muassasasi topilmadi")));
        assignment.setDirection(request.direction());
        assignment.setProposalNote(request.note());
        assignment.setProposedBy(currentUser.username());
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setCycleYear(LocalDate.now().getYear());
        Assignment saved = assignments.save(assignment);
        audit.record("PROPOSE", "Assignment", saved.getId(), unit.getName() + " <- " + saved.getInstitution().getName());
        notifications.notifyRoles(List.of(Role.SUPER_ADMIN), "Biriktirish taklifi",
                unit.getName() + " uchun " + saved.getInstitution().getName() + " taklif qilindi", "/assignments");
        return toDto(saved);
    }

    @Transactional
    public AssignmentDto startReview(Long id) {
        Assignment assignment = find(id);
        if (assignment.getStatus() != AssignmentStatus.PROPOSED) {
            throw new BusinessRuleException("Faqat yangi takliflarni ko'rib chiqishga olish mumkin");
        }
        assignment.setStatus(AssignmentStatus.UNDER_REVIEW);
        audit.record("REVIEW", "Assignment", id, "ko'rib chiqilmoqda");
        return toDto(assignment);
    }

    @Transactional
    public AssignmentDto decide(Long id, DecisionRequest request) {
        Assignment assignment = find(id);
        if (assignment.getStatus() == AssignmentStatus.APPROVED || assignment.getStatus() == AssignmentStatus.REJECTED) {
            throw new BusinessRuleException("Qaror allaqachon chiqarilgan");
        }
        if (request.approve() && (request.basisDocument() == null || request.basisDocument().isBlank())) {
            throw new BusinessRuleException("Tasdiqlash uchun asos hujjat (qo'shma qaror/buyruq) ko'rsatilishi shart");
        }
        assignment.setStatus(request.approve() ? AssignmentStatus.APPROVED : AssignmentStatus.REJECTED);
        assignment.setBasisDocument(request.basisDocument());
        assignment.setValidFrom(request.validFrom());
        assignment.setValidTo(request.validTo());
        assignment.setDecisionNote(request.note());
        assignment.setDecidedBy(currentUser.username());
        audit.record(request.approve() ? "APPROVE" : "REJECT", "Assignment", id, request.note());
        notifications.notifyUnitPermissionHolders(assignment.getMilitaryUnit().getId(), Permission.ASSIGNMENT_READ,
                "Biriktirish bo'yicha qaror",
                assignment.getInstitution().getName() + ": " + (request.approve() ? "tasdiqlandi" : "rad etildi"),
                "/assignments");
        return toDto(assignment);
    }

    @Transactional
    public AssignmentDto updateContract(Long id, ContractRequest request) {
        Assignment assignment = find(id);
        if (assignment.getStatus() != AssignmentStatus.APPROVED) {
            throw new BusinessRuleException("Shartnoma faqat tasdiqlangan biriktirish uchun kiritiladi");
        }
        assignment.setContractNo(request.contractNo());
        assignment.setContractDate(request.contractDate());
        assignment.setJointPlan(request.jointPlan());
        audit.record("CONTRACT", "Assignment", id, request.contractNo());
        return toDto(assignment);
    }

    public Assignment find(Long id) {
        Assignment assignment = assignments.findById(id).orElseThrow(() -> new NotFoundException("Biriktirish topilmadi"));
        currentUser.scope().require(assignment.getMilitaryUnit().getMilitaryDistrict().getId(),
                assignment.getMilitaryUnit().getId());
        return assignment;
    }

    private AssignmentDto toDto(Assignment a) {
        return new AssignmentDto(a.getId(), a.getMilitaryUnit().getId(), a.getMilitaryUnit().getName(),
                a.getInstitution().getId(), a.getInstitution().getName(), a.getDirection(), a.getStatus(),
                a.getBasisDocument(), a.getValidFrom(), a.getValidTo(), a.getContractNo(), a.getContractDate(),
                a.getJointPlan(), a.getProposalNote(), a.getDecisionNote(), a.getProposedBy(), a.getDecidedBy(),
                a.getCreatedAt());
    }
}
