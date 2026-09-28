package uz.askar.education.facilities;

import jakarta.persistence.Column;
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
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.organization.MilitaryUnit;

/** O'quv-moddiy baza xatlovi: sinf/o'quv joyi, jihozlar va holati (TT M4). */
@Entity
@Table(name = "facilities")
@Getter
@Setter
@NoArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;

    private String name;

    @Enumerated(EnumType.STRING)
    private FacilityKind kind;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_state")
    private FacilityCondition condition;

    private String equipment;
    private String shortages;
    private LocalDate surveyDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "facility_suitable", joinColumns = @JoinColumn(name = "facility_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private Set<DictionaryItem> suitableFor = new HashSet<>();
}
