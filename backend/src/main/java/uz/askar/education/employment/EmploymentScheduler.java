package uz.askar.education.employment;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.security.Role;

/** Xizmat tugashiga bir oy qolgan askarlar borligi haqida HKTBga haftalik bildirishnoma. */
@Component
@RequiredArgsConstructor
public class EmploymentScheduler {

    private final EmploymentService service;
    private final NotificationService notifications;

    @Scheduled(cron = "0 30 8 * * MON")
    public void weeklyReminder() {
        notifyIfReady(LocalDate.now());
    }

    public int notifyIfReady(LocalDate today) {
        int count = service.previewSystem(today);
        if (count > 0) {
            notifications.notifyRoles(List.of(Role.HKTB), "Bandlik ro'yxatlari tayyor",
                    "Xizmati tugashiga bir oy qolgan, kasbga o'qitilgan askarlar: " + count, "/employment");
        }
        return count;
    }
}
