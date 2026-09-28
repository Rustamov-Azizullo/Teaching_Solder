package uz.askar.education.deadlines;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.notifications.NotificationService;
import uz.askar.education.security.Role;

@Service
@RequiredArgsConstructor
public class DeadlineService {

    private static final int DEFAULT_REMIND_DAYS = 7;

    public record DeadlineRequest(@NotBlank @Size(max = 200) String name, @Size(max = 500) String description,
                                  @NotNull LocalDate deadlineDate, @NotNull Role responsibleRole,
                                  @NotNull Role escalationRole, @Min(0) @Max(60) Integer remindDaysBefore) {
    }

    public record DeadlineDto(Long id, String name, String description, LocalDate deadlineDate, Role responsibleRole,
                              Role escalationRole, int remindDaysBefore, DeadlineStatus status, boolean overdue,
                              long daysLeft, int cycleYear) {
    }

    private final DeadlineRepository deadlines;
    private final NotificationService notifications;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<DeadlineDto> list(int cycleYear) {
        return deadlines.findByCycleYearOrderByDeadlineDateAsc(cycleYear).stream().map(this::toDto).toList();
    }

    @Transactional
    public DeadlineDto create(DeadlineRequest request) {
        Deadline deadline = new Deadline();
        apply(deadline, request);
        deadline.setCycleYear(request.deadlineDate().getYear());
        Deadline saved = deadlines.save(deadline);
        audit.record("CREATE", "Deadline", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Transactional
    public DeadlineDto update(Long id, DeadlineRequest request) {
        Deadline deadline = find(id);
        apply(deadline, request);
        deadline.setEscalated(false);
        audit.record("UPDATE", "Deadline", id, deadline.getName());
        return toDto(deadline);
    }

    @Transactional
    public DeadlineDto complete(Long id) {
        Deadline deadline = find(id);
        deadline.setStatus(DeadlineStatus.DONE);
        audit.record("DONE", "Deadline", id, deadline.getName());
        return toDto(deadline);
    }

    /** Standart yillik muddatlarni yaratadi (algoritmdagi sanalar); yil uchun allaqachon bo'lsa, hech narsa qilmaydi. */
    @Transactional
    public void seedStandard(int year) {
        if (deadlines.countByCycleYear(year) > 0) {
            return;
        }
        add(year, "Texnikumlarni qismlarga biriktirish takliflari", 2, 1, Role.ADMIN, Role.SUPER_ADMIN);
        add(year, "Kun tartibi va vaqt taqsimoti", 3, 20, Role.SUPER_ADMIN, Role.MEGA_SUPER_ADMIN);
        add(year, "O'quv-moddiy baza xatlovi", 3, 30, Role.USER, Role.ADMIN);
        add(year, "Qo'shma qaror, kasblar ro'yxati va o'quv dasturlari", 4, 1, Role.SUPER_ADMIN, Role.MEGA_SUPER_ADMIN);
        add(year, "Kurslar boshlanishi", 5, 1, Role.USER, Role.ADMIN);
        add(year, "BMBA platformasida nomzodlarni ro'yxatdan o'tkazish", 6, 30, Role.ADMIN, Role.SUPER_ADMIN);
    }

    /** Kunlik ish: yaqinlashgan muddatlar uchun eslatma, o'tgan muddatlar uchun yuqori darajaga xabar. */
    @Transactional
    public int process(LocalDate today) {
        int sent = 0;
        for (Deadline deadline : deadlines.findByStatus(DeadlineStatus.OPEN)) {
            long daysLeft = ChronoUnit.DAYS.between(today, deadline.getDeadlineDate());
            if (daysLeft >= 0 && daysLeft <= deadline.getRemindDaysBefore() && !today.equals(deadline.getLastReminderOn())) {
                notifications.notifyRoles(List.of(deadline.getResponsibleRole()), "Muddat yaqinlashmoqda",
                        deadline.getName() + ": " + daysLeft + " kun qoldi (" + deadline.getDeadlineDate() + ")",
                        "/deadlines");
                deadline.setLastReminderOn(today);
                sent++;
            } else if (daysLeft < 0 && !deadline.isEscalated()) {
                notifications.notifyRoles(List.of(deadline.getEscalationRole(), deadline.getResponsibleRole()),
                        "Muddat o'tib ketdi", deadline.getName() + " bajarilmadi (" + deadline.getDeadlineDate() + ")",
                        "/deadlines");
                deadline.setEscalated(true);
                sent++;
            }
        }
        return sent;
    }

    private void add(int year, String name, int month, int day, Role responsible, Role escalation) {
        Deadline deadline = new Deadline();
        deadline.setName(name);
        deadline.setDeadlineDate(LocalDate.of(year, month, day));
        deadline.setResponsibleRole(responsible);
        deadline.setEscalationRole(escalation);
        deadline.setRemindDaysBefore(DEFAULT_REMIND_DAYS);
        deadline.setCycleYear(year);
        deadlines.save(deadline);
    }

    private void apply(Deadline deadline, DeadlineRequest request) {
        deadline.setName(request.name());
        deadline.setDescription(request.description());
        deadline.setDeadlineDate(request.deadlineDate());
        deadline.setResponsibleRole(request.responsibleRole());
        deadline.setEscalationRole(request.escalationRole());
        deadline.setRemindDaysBefore(request.remindDaysBefore() == null ? DEFAULT_REMIND_DAYS : request.remindDaysBefore());
    }

    private Deadline find(Long id) {
        return deadlines.findById(id).orElseThrow(() -> new NotFoundException("Muddat topilmadi"));
    }

    private DeadlineDto toDto(Deadline d) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), d.getDeadlineDate());
        return new DeadlineDto(d.getId(), d.getName(), d.getDescription(), d.getDeadlineDate(), d.getResponsibleRole(),
                d.getEscalationRole(), d.getRemindDaysBefore(), d.getStatus(),
                d.getStatus() == DeadlineStatus.OPEN && daysLeft < 0, daysLeft, d.getCycleYear());
    }
}
