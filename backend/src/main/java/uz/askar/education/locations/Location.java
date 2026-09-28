package uz.askar.education.locations;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryUnit;

/**
 * Foydalanuvchini hududga biriktirish uchun yagona daraxt: respublika -&gt; okrug -&gt; qism.
 * Okrug va qism yozuvlari mavjud {@code military_districts}/{@code military_units} ga orqaga havola saqlaydi,
 * shuning uchun barcha mavjud districtId/unitId filtrlari o'zgarishsiz ishlaydi.
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @Enumerated(EnumType.STRING)
    private LocationLevel level;

    private String name;
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "military_district_id")
    private MilitaryDistrict militaryDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;

    /** Okrug darajasida — o'z okrugi, qism darajasida — ota (okrug) yozuvining okrugi, respublikada — {@code null}. */
    public Long districtId() {
        return switch (level) {
            case REPUBLIC -> null;
            case DISTRICT -> militaryDistrict.getId();
            case UNIT -> parent.getMilitaryDistrict().getId();
        };
    }

    /** Faqat qism darajasida qism identifikatori, aks holda {@code null}. */
    public Long unitId() {
        return level == LocationLevel.UNIT ? militaryUnit.getId() : null;
    }
}
