package uz.askar.education.soldiers;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.Region;
import uz.askar.education.organization.TerritorialDistrict;

/** Askarning shaxsiy yig'ma jildi (TT M2, anketa I–III bo'limlari). */
@Entity
@Table(name = "soldiers")
@Getter
@Setter
@NoArgsConstructor
public class Soldier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pinfl;
    private String fullName;
    private LocalDate birthDate;
    private String passport;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_kinship_id")
    private DictionaryItem phoneKinship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id")
    private TerritorialDistrict district;

    private String mahalla;
    private String street;
    private String house;
    private String apartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subdivision_id")
    private uz.askar.education.organization.Subdivision subdivision;

    private LocalDate conscriptionDate;
    private LocalDate serviceEndDate;

    @Enumerated(EnumType.STRING)
    private GeneralEducation generalEducation;

    @Enumerated(EnumType.STRING)
    private ProfessionalEducation professionalEducation;

    @Enumerated(EnumType.STRING)
    private HigherEducation higherEducation;

    private String priorOccupation;
    private boolean noPriorOccupation;

    /** Kasbga o'quvchanligi haqida belgi. */
    private Boolean trainable;

    private int cycleYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "soldier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SoldierCertificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "soldier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SoldierAward> awards = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "soldier_field_sources", joinColumns = @JoinColumn(name = "soldier_id"))
    @MapKeyColumn(name = "field_name")
    private Map<String, FieldSourceInfo> fieldSources = new HashMap<>();
}
