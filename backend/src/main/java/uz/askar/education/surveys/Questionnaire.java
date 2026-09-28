package uz.askar.education.surveys;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.users.AppUser;

/** Elektron anketa: IV (qiziqishlar) va V (oliy ta'lim rejalari) bo'limlari — so'rovnoma natijalari. */
@Entity
@Table(name = "questionnaires")
@Getter
@Setter
@NoArgsConstructor
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soldier_id")
    private Soldier soldier;

    private int cycleYear;

    @Enumerated(EnumType.STRING)
    private QuestionnaireStatus status = QuestionnaireStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "psychologist_id")
    private AppUser psychologist;

    private LocalDate filledDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_direction_id")
    private DictionaryItem interestDirection;

    private String interestOtherText;
    private String planOtherText;
    private String otherSubjectText;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "questionnaire_plans", joinColumns = @JoinColumn(name = "questionnaire_id"),
            inverseJoinColumns = @JoinColumn(name = "plan_id"))
    private Set<DictionaryItem> futurePlans = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "questionnaire_specialty_subjects", joinColumns = @JoinColumn(name = "questionnaire_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<DictionaryItem> specialtySubjects = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "questionnaire_mandatory_subjects", joinColumns = @JoinColumn(name = "questionnaire_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<DictionaryItem> mandatorySubjects = new HashSet<>();

    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UniversityChoice> universityChoices = new ArrayList<>();

    public boolean isFinalized() {
        return status == QuestionnaireStatus.FINALIZED;
    }
}
