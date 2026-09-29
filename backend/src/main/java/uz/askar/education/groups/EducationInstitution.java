package uz.askar.education.groups;

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
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.Region;

@Entity
@Table(name = "education_institutions")
@Getter
@Setter
@NoArgsConstructor
public class EducationInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InstitutionType type;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    /** Shartnoma tuzilgan harbiy qismlar: o'qituvchi faqat shular uchun shu muassasadan qo'shiladi. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "institution_units", joinColumns = @JoinColumn(name = "institution_id"),
            inverseJoinColumns = @JoinColumn(name = "military_unit_id"))
    private Set<MilitaryUnit> contractedUnits = new HashSet<>();

    /** Muassasada o'qitiladigan kasblar va fanlar (ma'lumotnoma yozuvlari). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "institution_specialties", joinColumns = @JoinColumn(name = "institution_id"),
            inverseJoinColumns = @JoinColumn(name = "dictionary_item_id"))
    private Set<DictionaryItem> specialties = new HashSet<>();
}
