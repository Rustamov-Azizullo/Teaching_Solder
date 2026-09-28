package uz.askar.education.deadlines;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.security.Role;

/** Algoritmdagi nazorat muddati (TT M13). */
@Entity
@Table(name = "deadlines")
@Getter
@Setter
@NoArgsConstructor
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    private Role responsibleRole;

    @Enumerated(EnumType.STRING)
    private Role escalationRole;

    private int remindDaysBefore = 7;

    @Enumerated(EnumType.STRING)
    private DeadlineStatus status = DeadlineStatus.OPEN;

    private LocalDate lastReminderOn;
    private boolean escalated;
    private int cycleYear;
}
