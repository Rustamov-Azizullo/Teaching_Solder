package uz.askar.education.admissions;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.integrations.BmbaClient;
import uz.askar.education.integrations.IntegrationGateway;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierService;

@Service
@RequiredArgsConstructor
public class AdmissionService {

    private static final int RESERVE_LOOKAHEAD_MONTHS = 2;

    public record AdmissionUpdate(boolean bmbaRegistered, boolean benefitsUploaded, boolean testParticipated,
                                  Double testScore, boolean admitted, @Size(max = 255) String university,
                                  @Size(max = 255) String studyDirection, StudyForm studyForm,
                                  OnlineStatus onlineStatus) {
    }

    public record AdmissionRow(Long soldierId, String fullName, String pinfl, Long districtId, String districtName,
                               Long unitId, String unitName, boolean bmbaRegistered,
                               boolean benefitsUploaded, boolean testParticipated, Double testScore, boolean admitted,
                               String university, String studyDirection, StudyForm studyForm, OnlineStatus onlineStatus,
                               LocalDate serviceEndDate, LocalDateTime bmbaSyncedAt) {
    }

    public record FunnelStep(String label, long count) {
    }

    public record SyncResult(int synced, int notFound, int failed) {
    }

    private final AdmissionRepository admissions;
    private final SoldierService soldierService;
    private final uz.askar.education.cycles.CycleService cycleService;
    private final uz.askar.education.soldiers.SoldierRepository soldiers;
    private final BmbaClient bmbaClient;
    private final IntegrationGateway gateway;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<AdmissionRow> list() {
        Map<Long, Admission> byId = existing();
        return candidates().stream().map(s -> toRow(s, byId.get(s.getId()))).toList();
    }

    @Transactional
    public AdmissionRow update(Long soldierId, AdmissionUpdate update) {
        cycleService.requireCurrentOpen();
        Soldier soldier = soldierService.findInScope(soldierId);
        if (update.admitted() && (update.university() == null || update.university().isBlank())) {
            throw new BusinessRuleException("Qabul qilingan bo'lsa, OTM nomi ko'rsatilishi shart");
        }
        Admission admission = getOrCreate(soldier);
        admission.setBmbaRegistered(update.bmbaRegistered());
        admission.setBenefitsUploaded(update.benefitsUploaded());
        admission.setTestParticipated(update.testParticipated());
        admission.setTestScore(update.testScore());
        admission.setAdmitted(update.admitted());
        admission.setUniversity(update.university());
        admission.setStudyDirection(update.studyDirection());
        admission.setStudyForm(update.studyForm());
        admission.setOnlineStatus(update.onlineStatus() == null ? OnlineStatus.NONE : update.onlineStatus());
        touch(admission);
        audit.record("UPDATE", "Admission", soldierId, "qabul: " + update.admitted());
        return toRow(soldier, admission);
    }

    /** BMBA API dan holatni oladi; API javob bermagan askar uchun qo'lda kiritilgan qiymat saqlanib qoladi. */
    @Transactional
    public SyncResult syncFromBmba() {
        int synced = 0;
        int notFound = 0;
        int failed = 0;
        for (Soldier soldier : candidates()) {
            try {
                var status = gateway.call("BMBA", "STATUS", soldier.getPinfl().substring(0, 3) + "***",
                        () -> bmbaClient.findByPinfl(soldier.getPinfl()));
                if (status.isEmpty()) {
                    notFound++;
                    continue;
                }
                Admission admission = getOrCreate(soldier);
                var s = status.get();
                admission.setBmbaRegistered(s.registered());
                admission.setBenefitsUploaded(s.benefitsUploaded());
                admission.setTestParticipated(s.testScore() != null);
                admission.setTestScore(s.testScore());
                admission.setAdmitted(s.admitted());
                admission.setUniversity(s.university());
                admission.setStudyDirection(s.direction());
                admission.setBmbaSyncedAt(LocalDateTime.now());
                touch(admission);
                synced++;
            } catch (RuntimeException ex) {
                failed++;
            }
        }
        audit.record("BMBA_SYNC", "Admission", null, "yangilandi: " + synced + ", topilmadi: " + notFound + ", xato: " + failed);
        return new SyncResult(synced, notFound, failed);
    }

    /** Voronka: nomzodlar → BMBAda ro'yxatdan o'tganlar → testda qatnashganlar → qabul qilinganlar. */
    @Transactional(readOnly = true)
    public List<FunnelStep> funnel() {
        List<AdmissionRow> rows = list();
        return List.of(
                new FunnelStep("Nomzodlar", rows.size()),
                new FunnelStep("BMBAda ro'yxatdan o'tganlar", rows.stream().filter(AdmissionRow::bmbaRegistered).count()),
                new FunnelStep("Testda qatnashganlar", rows.stream().filter(AdmissionRow::testParticipated).count()),
                new FunnelStep("Qabul qilinganlar", rows.stream().filter(AdmissionRow::admitted).count()));
    }

    /** Fevralda muddatidan bir oy oldin zaxiraga bo'shatish uchun ro'yxat: qabul qilinganlar, xizmati tez tugaydiganlar. */
    @Transactional(readOnly = true)
    public List<AdmissionRow> reserveList(LocalDate today) {
        LocalDate limit = today.plusMonths(RESERVE_LOOKAHEAD_MONTHS);
        return list().stream().filter(AdmissionRow::admitted)
                .filter(r -> r.serviceEndDate() != null && !r.serviceEndDate().isAfter(limit)).toList();
    }

    private List<Soldier> candidates() {
        var scope = currentUser.scope();
        return admissions.findCandidates(scope.districtFilter(), scope.unitFilter());
    }

    private Map<Long, Admission> existing() {
        return admissions.findByCycleYear(LocalDate.now().getYear()).stream()
                .collect(Collectors.toMap(a -> a.getSoldier().getId(), Function.identity()));
    }

    private Admission getOrCreate(Soldier soldier) {
        return admissions.findBySoldierIdAndCycleYear(soldier.getId(), LocalDate.now().getYear()).orElseGet(() -> {
            Admission created = new Admission();
            created.setSoldier(soldier);
            created.setCycleYear(LocalDate.now().getYear());
            return created;
        });
    }

    private void touch(Admission admission) {
        admission.setUpdatedAt(LocalDateTime.now());
        admission.setUpdatedBy(currentUser.username());
        admissions.save(admission);
    }

    private AdmissionRow toRow(Soldier s, Admission a) {
        boolean has = a != null;
        return new AdmissionRow(s.getId(), s.getFullName(), s.getPinfl(),
                s.getMilitaryUnit().getMilitaryDistrict().getId(), s.getMilitaryUnit().getMilitaryDistrict().getName(),
                s.getMilitaryUnit().getId(), s.getMilitaryUnit().getName(),
                has && a.isBmbaRegistered(), has && a.isBenefitsUploaded(), has && a.isTestParticipated(),
                has ? a.getTestScore() : null, has && a.isAdmitted(), has ? a.getUniversity() : null,
                has ? a.getStudyDirection() : null, has ? a.getStudyForm() : null,
                has ? a.getOnlineStatus() : OnlineStatus.NONE, s.getServiceEndDate(), has ? a.getBmbaSyncedAt() : null);
    }
}
