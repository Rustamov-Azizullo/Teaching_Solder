package uz.askar.education.retention;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.attachments.Attachment;
import uz.askar.education.attachments.AttachmentOwnerType;
import uz.askar.education.attachments.AttachmentRepository;
import uz.askar.education.attachments.EncryptedFileStore;
import uz.askar.education.audit.AuditService;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.security.Role;
import uz.askar.education.settings.SettingsService;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierRepository;

/**
 * Ma'lumotlarni saqlash muddati (sozlanadigan). Muddat tugaganda yozuvlar anonimlashtiriladi (statistika saqlanadi).
 * Anonimlashtirishdan {@link #GRACE_DAYS} kun oldin administratorga ogohlantirish yuboriladi va amal audit jurnaliga yoziladi.
 */
@Service
@RequiredArgsConstructor
public class RetentionService {

    static final int GRACE_DAYS = 7;
    private static final String ANONYMOUS_PREFIX = "A";

    private final SoldierRepository soldiers;
    private final AttachmentRepository attachments;
    private final EncryptedFileStore store;
    private final SettingsService settings;
    private final NotificationService notifications;
    private final AuditService audit;

    @Scheduled(cron = "0 0 3 * * *")
    public void dailyJob() {
        warn(LocalDate.now());
        anonymizeDue(LocalDate.now());
    }

    /** Ertaga ham shu ogohlantirish qayta yuborilmasligi uchun faqat muddati aynan bugun yetganlar haqida yuboriladi. */
    @Transactional
    public int warn(LocalDate today) {
        int years = years();
        if (years <= 0) {
            return 0;
        }
        LocalDate warnDate = today.minusYears(years);
        long count = soldiers.findServiceEndedOn(warnDate).stream().filter(s -> !isAnonymous(s)).count();
        if (count > 0) {
            notifications.notifyRoles(List.of(Role.SYSTEM_ADMIN), "Ma'lumotlar anonimlashtiriladi",
                    count + " ta askar ma'lumotlari saqlash muddati tugadi; " + GRACE_DAYS
                            + " kundan keyin anonimlashtiriladi", "/settings");
        }
        return (int) count;
    }

    @Transactional
    public int anonymizeDue(LocalDate today) {
        int years = years();
        if (years <= 0) {
            return 0;
        }
        LocalDate cutoff = today.minusYears(years).minusDays(GRACE_DAYS);
        List<Soldier> due = soldiers.findServiceEndedBefore(cutoff).stream().filter(s -> !isAnonymous(s)).toList();
        due.forEach(this::anonymize);
        if (!due.isEmpty()) {
            audit.record("system", "ANONYMIZE", "Soldier", null, "anonimlashtirildi: " + due.size());
        }
        return due.size();
    }

    private void anonymize(Soldier soldier) {
        soldier.setFullName("Anonim");
        soldier.setPinfl(String.format("%s%013d", ANONYMOUS_PREFIX, soldier.getId()));
        soldier.setPassport("XX0000000");
        soldier.setPhone("+998000000000");
        soldier.setMahalla(null);
        soldier.setStreet(null);
        soldier.setHouse(null);
        soldier.setApartment(null);
        soldier.setPriorOccupation(null);
        soldier.getFieldSources().clear();
        for (Attachment attachment : attachments.findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(
                AttachmentOwnerType.SOLDIER, soldier.getId())) {
            store.delete(attachment.getStorageName());
            attachments.delete(attachment);
        }
    }

    private boolean isAnonymous(Soldier soldier) {
        return soldier.getPinfl().startsWith(ANONYMOUS_PREFIX);
    }

    private int years() {
        try {
            return Integer.parseInt(settings.get("retention.years", "0"));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
