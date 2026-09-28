package uz.askar.education.attendance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.attendance.AttendanceDtos.AttendanceEntryInput;
import uz.askar.education.attendance.AttendanceDtos.AttendanceRequest;
import uz.askar.education.attendance.AttendanceDtos.AttendanceSheet;
import uz.askar.education.attendance.AttendanceDtos.RosterEntry;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.schedule.Lesson;
import uz.askar.education.schedule.LessonService;
import uz.askar.education.schedule.LessonStatus;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Role;
import uz.askar.education.settings.SettingsService;
import uz.askar.education.soldiers.Soldier;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendances;
    private final LessonService lessonService;
    private final SettingsService settings;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public AttendanceSheet sheet(Long lessonId) {
        Lesson lesson = lessonService.findInScope(lessonId);
        Map<Long, Attendance> existing = attendances.findByLessonId(lessonId).stream()
                .collect(Collectors.toMap(a -> a.getSoldier().getId(), a -> a));
        List<RosterEntry> roster = lesson.getGroup().getSoldiers().stream()
                .sorted(Comparator.comparing(Soldier::getFullName))
                .map(soldier -> toRosterEntry(soldier, existing.get(soldier.getId())))
                .toList();
        String lockReason = lockReason(lesson);
        return new AttendanceSheet(lessonService.toDto(lesson), roster, lockReason == null, lockReason);
    }

    @Transactional
    public AttendanceSheet record(Long lessonId, AttendanceRequest request) {
        Lesson lesson = lessonService.findInScope(lessonId);
        String lockReason = lockReason(lesson);
        if (lockReason != null) {
            throw new BusinessRuleException(lockReason);
        }
        Set<Long> memberIds = lesson.getGroup().getSoldiers().stream().map(Soldier::getId)
                .collect(Collectors.toSet());
        validateEntries(request.entries(), memberIds);

        Map<Long, Attendance> existing = attendances.findByLessonId(lessonId).stream()
                .collect(Collectors.toMap(a -> a.getSoldier().getId(), a -> a));
        Map<Long, Soldier> soldiersById = lesson.getGroup().getSoldiers().stream()
                .collect(Collectors.toMap(Soldier::getId, s -> s));
        boolean isCorrection = lesson.isAttendanceRecorded();
        for (AttendanceEntryInput entry : request.entries()) {
            Attendance attendance = existing.getOrDefault(entry.soldierId(), new Attendance());
            attendance.setLesson(lesson);
            attendance.setSoldier(soldiersById.get(entry.soldierId()));
            attendance.setStatus(entry.status());
            attendance.setReason(entry.status() == AttendanceStatus.ABSENT ? entry.reason() : null);
            attendance.setRecordedBy(currentUser.username());
            attendances.save(attendance);
        }
        markLessonHeld(lesson, request);
        audit.record(isCorrection ? "UPDATE_ATTENDANCE" : "RECORD_ATTENDANCE", "Lesson", lessonId,
                "keldi=" + request.entries().stream().filter(e -> e.status() == AttendanceStatus.PRESENT).count()
                        + ", kelmadi=" + request.entries().stream()
                        .filter(e -> e.status() == AttendanceStatus.ABSENT).count());
        return sheet(lessonId);
    }

    private void markLessonHeld(Lesson lesson, AttendanceRequest request) {
        lesson.setTeacherPresent(request.teacherPresent());
        if (request.topic() != null && !request.topic().isBlank()) {
            lesson.setTopic(request.topic());
        }
        lesson.setStatus(LessonStatus.HELD);
        if (!lesson.isAttendanceRecorded()) {
            lesson.setAttendanceRecorded(true);
            lesson.setAttendanceRecordedAt(LocalDateTime.now());
        }
    }

    private void validateEntries(List<AttendanceEntryInput> entries, Set<Long> memberIds) {
        Set<Long> seen = new HashSet<>();
        for (AttendanceEntryInput entry : entries) {
            if (!memberIds.contains(entry.soldierId())) {
                throw new BusinessRuleException("Askar guruh a'zosi emas");
            }
            if (!seen.add(entry.soldierId())) {
                throw new BusinessRuleException("Bir askar davomatda ikki marta ko'rsatilgan");
            }
            if (entry.status() == AttendanceStatus.ABSENT && entry.reason() == null) {
                throw new BusinessRuleException("Kelmagan askar uchun sabab ko'rsatilishi shart");
            }
        }
        if (!seen.equals(memberIds)) {
            throw new BusinessRuleException("Guruhning barcha askarlari uchun davomat kiritilishi kerak");
        }
    }

    /**
     * Davomatni kiritish/tahrirlash mumkin emasligi sababi ({@code null} — mumkin).
     * Belgilangan muddatdan keyin faqat qism qo'mondoni (yoki administrator) tuzatishi mumkin.
     */
    private String lockReason(Lesson lesson) {
        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            return "Mashg'ulot bekor qilingan";
        }
        if (lesson.getLessonDate().isAfter(LocalDate.now())) {
            return "Kelajakdagi mashg'ulotga davomat kiritib bo'lmaydi";
        }
        if (lesson.isAttendanceRecorded() && isEditWindowClosed(lesson)
                && !currentUser.hasRole(Role.UNIT_COMMANDER, Role.SYSTEM_ADMIN)) {
            return "Tahrirlash muddati tugagan. Faqat qism qo'mondoni tuzatishi mumkin";
        }
        return null;
    }

    private boolean isEditWindowClosed(Lesson lesson) {
        Duration window = Duration.ofHours(settings.attendanceEditHours());
        return lesson.getAttendanceRecordedAt().plus(window).isBefore(LocalDateTime.now());
    }

    private RosterEntry toRosterEntry(Soldier soldier, Attendance attendance) {
        return new RosterEntry(soldier.getId(), soldier.getFullName(),
                attendance == null ? null : attendance.getStatus(),
                attendance == null ? null : attendance.getReason());
    }
}
