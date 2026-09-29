package uz.askar.education.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.users.AppUserRepository;

/** Okrug foydalanuvchilariga har hafta boshida haftalik hisobotni yuklab olish haqida eslatma yuboradi. */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private static final String REPORT_TITLE = "Haftalik hisobot tayyor";
    private static final String REPORT_BODY =
            "Harbiy qismlar kesimida haftalik hisobotni «Hisobotlar» sahifasidan yuklab oling";
    private static final String REPORTS_LINK = "/reports";

    private final AppUserRepository users;
    private final PermissionEvaluatorService permissions;
    private final NotificationService notifications;

    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void remindDistricts() {
        var recipients = users.findInDistrictScope(null).stream()
                .filter(user -> user.isActive() && user.effectiveDistrictId() != null)
                .filter(user -> permissions.holds(user.getId(), user.getRole(), Permission.REPORTS))
                .toList();
        recipients.forEach(user -> notifications.notifyUser(user.getId(), REPORT_TITLE, REPORT_BODY, REPORTS_LINK));
        log.info("Haftalik hisobot eslatmasi: {} ta foydalanuvchi", recipients.size());
    }
}
