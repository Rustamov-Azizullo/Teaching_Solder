package uz.askar.education.soldiers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.groups.StudyGroupRepository;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.Subdivision;
import uz.askar.education.organization.SubdivisionService;
import uz.askar.education.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class SoldierTransferService {

    public record TransferRequest(@NotNull Long militaryUnitId, Long subdivisionId,
                                  @NotBlank @Size(max = 255) String reason) {
    }

    public record TransferDto(Long id, String fromUnit, String toUnit, String fromSubdivision, String toSubdivision,
                              String reason, LocalDateTime transferredAt, String transferredBy) {
    }

    private final SoldierService soldierService;
    private final SoldierTransferRepository transfers;
    private final MilitaryUnitRepository militaryUnits;
    private final SubdivisionService subdivisionService;
    private final StudyGroupRepository groups;
    private final uz.askar.education.results.CourseResultRepository courseResults;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional
    public TransferDto transfer(Long soldierId, TransferRequest request) {
        Soldier soldier = soldierService.findInScope(soldierId);
        var target = militaryUnits.findById(request.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        Subdivision targetSubdivision = null;
        if (request.subdivisionId() != null) {
            targetSubdivisionCheck(target.getId(), request.subdivisionId());
        }
        boolean sameUnit = target.getId().equals(soldier.getMilitaryUnit().getId());
        if (sameUnit && request.subdivisionId() == null) {
            throw new BusinessRuleException("Boshqa qism yoki bo'linma tanlang");
        }

        SoldierTransfer record = new SoldierTransfer();
        record.setSoldier(soldier);
        record.setFromUnit(soldier.getMilitaryUnit());
        record.setToUnit(target);
        record.setFromSubdivision(soldier.getSubdivision());
        record.setReason(request.reason());
        record.setTransferredAt(LocalDateTime.now());
        record.setTransferredBy(currentUser.username());

        if (!sameUnit) {
            // Eski qismning guruhlaridan chiqariladi; yig'ma jild o'zi bilan ko'chadi.
            groups.findBySoldierId(soldierId).forEach(group -> {
                if (group.getCourseApprovedAt() == null) {
                    courseResults.deleteByGroupIdAndSoldierIdIn(group.getId(), java.util.Set.of(soldierId));
                }
                group.getSoldiers().remove(soldier);
            });
        }
        if (request.subdivisionId() != null) {
            targetSubdivision = subdivisionService.findInScopeAnyUnit(request.subdivisionId());
        }
        soldier.setMilitaryUnit(target);
        soldier.setSubdivision(targetSubdivision);
        record.setToSubdivision(targetSubdivision);
        SoldierTransfer saved = transfers.save(record);
        audit.record("TRANSFER", "Soldier", soldierId, record.getFromUnit().getName() + " -> " + target.getName());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TransferDto> history(Long soldierId) {
        soldierService.findInScope(soldierId);
        return transfers.findBySoldierIdOrderByTransferredAtDesc(soldierId).stream().map(this::toDto).toList();
    }

    private void targetSubdivisionCheck(Long unitId, Long subdivisionId) {
        Subdivision subdivision = subdivisionService.findInScopeAnyUnit(subdivisionId);
        if (!subdivision.getMilitaryUnit().getId().equals(unitId)) {
            throw new BusinessRuleException("Bo'linma tanlangan qismga tegishli emas");
        }
    }

    private TransferDto toDto(SoldierTransfer t) {
        return new TransferDto(t.getId(), t.getFromUnit().getName(), t.getToUnit().getName(),
                t.getFromSubdivision() == null ? null : t.getFromSubdivision().path(),
                t.getToSubdivision() == null ? null : t.getToSubdivision().path(), t.getReason(),
                t.getTransferredAt(), t.getTransferredBy());
    }
}
