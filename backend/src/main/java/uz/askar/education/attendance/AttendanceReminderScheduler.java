package uz.askar.education.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.schedule.Lesson;
import uz.askar.education.schedule.LessonRepository;
import uz.askar.education.schedule.LessonStatus;
import uz.askar.education.security.Role;

/** Kun oxirida davomat kiritilmagan guruhlar bo'yicha guruh kattasi va qism qo'mondoniga eslatma (TT M8). */
@Component
@RequiredArgsConstructor
public class AttendanceReminderScheduler {

    private final LessonRepository lessons;
    private final NotificationService notifications;

    @Scheduled(cron = "0 35 17 * * MON-FRI")
    public void dailyReminder() {
        remindMissing(LocalDate.now());
    }

    @Transactional
    public int remindMissing(LocalDate date) {
        List<Lesson> missing = lessons.findByDateInScope(date, null, null, null).stream()
                .filter(l -> l.getStatus() != LessonStatus.CANCELLED && !l.isAttendanceRecorded()).toList();
        missing.stream().filter(l -> l.getGroup().getLeader() != null).forEach(l -> notifications.notifyUser(
                l.getGroup().getLeader().getId(), "Davomat kiritilmagan",
                l.getGroup().getName() + " guruhi uchun bugungi davomatni kiriting", "/attendance/" + l.getId()));
        Map<Long, Long> perUnit = missing.stream()
                .collect(Collectors.groupingBy(l -> l.getGroup().getMilitaryUnit().getId(), Collectors.counting()));
        perUnit.forEach((unitId, count) -> notifications.notifyUnit(unitId, List.of(Role.UNIT_COMMANDER),
                "Davomat kiritilmagan guruhlar", count + " ta guruhda bugungi davomat kiritilmagan", "/attendance"));
        return missing.size();
    }
}
