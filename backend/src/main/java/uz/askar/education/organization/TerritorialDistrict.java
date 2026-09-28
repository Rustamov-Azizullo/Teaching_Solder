package uz.askar.education.organization;

import jakarta.persistence.Entity;
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

/** Viloyat tarkibidagi tuman yoki shahar. Harbiy okrugdan farqlash uchun "Territorial" deb nomlangan. */
@Entity
@Table(name = "districts")
@Getter
@Setter
@NoArgsConstructor
public class TerritorialDistrict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id")
    private Region region;

    private String name;
}
