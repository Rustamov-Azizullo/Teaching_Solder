package uz.askar.education.results;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.groups.GroupService;
import uz.askar.education.groups.GroupType;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Role;
import uz.askar.education.soldiers.Soldier;

@Service
@RequiredArgsConstructor
public class ResultService {

    public record ResultInput(@NotNull Long soldierId, @NotNull CourseStatus status,
                              @Min(0) @Max(100) Integer examGrade, @Size(max = 255) String dropReason,
                              @Size(max = 60) String certificateNo, LocalDate certificateDate,
                              @Size(max = 160) String certificateIssuer) {
    }

    public record ResultsRequest(@NotEmpty @Valid List<ResultInput> entries) {
    }

    public record ResultRow(Long soldierId, String fullName, String pinfl, CourseStatus status, Integer examGrade,
                            String dropReason, String certificateNo, LocalDate certificateDate,
                            String certificateIssuer) {
    }

    public record ResultsSheet(Long groupId, String groupName, GroupType type, boolean approved, String approvedBy,
                               LocalDateTime approvedAt, List<ResultRow> rows) {
    }

    private final CourseResultRepository results;
    private final GroupService groupService;
    private final uz.askar.education.cycles.CycleService cycleService;
    private final NotificationService notifications;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public ResultsSheet sheet(Long groupId) {
        StudyGroup group = groupService.findInScope(groupId);
        Map<Long, CourseResult> existing = results.findByGroupId(groupId).stream()
                .collect(Collectors.toMap(r -> r.getSoldier().getId(), r -> r));
        List<ResultRow> rows = group.getSoldiers().stream().sorted(Comparator.comparing(Soldier::getFullName))
                .map(s -> toRow(s, existing.get(s.getId()))).toList();
        return new ResultsSheet(groupId, group.getName(), group.getType(), group.getCourseApprovedAt() != null,
                group.getCourseApprovedBy(), group.getCourseApprovedAt(), rows);
    }

    @Transactional
    public ResultsSheet save(Long groupId, ResultsRequest request) {
        StudyGroup group = groupService.findInScope(groupId);
        cycleService.requireOpen(group.getCycleYear());
        if (group.getType() != GroupType.VOCATIONAL) {
            throw new BusinessRuleException("Kurs natijalari faqat kasb kursi guruhlari uchun kiritiladi");
        }
        if (group.getCourseApprovedAt() != null) {
            throw new BusinessRuleException("Kurs yakuni tasdiqlangan — natijalarni o'zgartirib bo'lmaydi");
        }
        Map<Long, Soldier> members = group.getSoldiers().stream().collect(Collectors.toMap(Soldier::getId, s -> s));
        Map<Long, CourseResult> existing = results.findByGroupId(groupId).stream()
                .collect(Collectors.toMap(r -> r.getSoldier().getId(), r -> r));
        Map<Long, CourseResult> toSave = new HashMap<>();
        for (ResultInput input : request.entries()) {
            Soldier soldier = members.get(input.soldierId());
            if (soldier == null) {
                throw new BusinessRuleException("Askar guruh a'zosi emas");
            }
            validate(input, soldier);
            CourseResult result = existing.getOrDefault(input.soldierId(), new CourseResult());
            result.setGroup(group);
            result.setSoldier(soldier);
            result.setStatus(input.status());
            result.setExamGrade(input.examGrade());
            result.setDropReason(input.status() == CourseStatus.DROPPED ? input.dropReason() : null);
            boolean certified = input.status() == CourseStatus.CERTIFIED;
            result.setCertificateNo(certified ? input.certificateNo() : null);
            result.setCertificateDate(certified ? input.certificateDate() : null);
            result.setCertificateIssuer(certified ? input.certificateIssuer() : null);
            result.setRecordedBy(currentUser.username());
            result.setUpdatedAt(LocalDateTime.now());
            toSave.put(input.soldierId(), result);
        }
        results.saveAll(toSave.values());
        audit.record("SAVE_RESULTS", "StudyGroup", groupId, "natijalar: " + toSave.size());
        return sheet(groupId);
    }

    /** Qism qo'mondoni kurs yakunini tasdiqlaydi → ma'lumot vazirlik dashboardida aks etadi. */
    @Transactional
    public ResultsSheet approve(Long groupId) {
        StudyGroup group = groupService.findInScope(groupId);
        cycleService.requireOpen(group.getCycleYear());
        if (group.getCourseApprovedAt() != null) {
            throw new BusinessRuleException("Kurs yakuni allaqachon tasdiqlangan");
        }
        Set<Long> recorded = results.findByGroupId(groupId).stream().map(r -> r.getSoldier().getId())
                .collect(Collectors.toSet());
        boolean complete = group.getSoldiers().stream().allMatch(s -> recorded.contains(s.getId()));
        if (group.getSoldiers().isEmpty() || !complete) {
            throw new BusinessRuleException("Tasdiqlashdan oldin guruhning barcha askarlari natijasi kiritilishi kerak");
        }
        group.setCourseApprovedAt(LocalDateTime.now());
        group.setCourseApprovedBy(currentUser.username());
        audit.record("APPROVE_COURSE", "StudyGroup", groupId, group.getName());
        notifications.notifyRoles(List.of(Role.SUPER_ADMIN), "Kurs yakuni tasdiqlandi",
                group.getMilitaryUnit().getName() + ": " + group.getName(), "/groups/" + groupId);
        return sheet(groupId);
    }

    private void validate(ResultInput input, Soldier soldier) {
        String who = soldier.getFullName();
        if (input.status() == CourseStatus.DROPPED && isBlank(input.dropReason())) {
            throw new BusinessRuleException(who + ": o'qishni tugatmaslik sababi ko'rsatilishi shart");
        }
        if (input.status() == CourseStatus.CERTIFIED
                && (isBlank(input.certificateNo()) || input.certificateDate() == null)) {
            throw new BusinessRuleException(who + ": sertifikat raqami va sanasi kiritilishi shart");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private ResultRow toRow(Soldier s, CourseResult r) {
        return new ResultRow(s.getId(), s.getFullName(), s.getPinfl(), r == null ? null : r.getStatus(),
                r == null ? null : r.getExamGrade(), r == null ? null : r.getDropReason(),
                r == null ? null : r.getCertificateNo(), r == null ? null : r.getCertificateDate(),
                r == null ? null : r.getCertificateIssuer());
    }
}
