package uz.askar.education.assignments;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.groups.EducationInstitution;
import uz.askar.education.groups.GroupType;
import uz.askar.education.organization.MilitaryUnit;

/** Muassasani (texnikum/maktab/o'quv markazi) harbiy qismga biriktirish (TT M3). */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id")
    private EducationInstitution institution;

    @Enumerated(EnumType.STRING)
    private GroupType direction;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.PROPOSED;

    private String basisDocument;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String contractNo;
    private LocalDate contractDate;
    private String jointPlan;
    private String proposalNote;
    private String decisionNote;
    private String proposedBy;
    private String decidedBy;
    private LocalDateTime createdAt;
    private int cycleYear;
}
