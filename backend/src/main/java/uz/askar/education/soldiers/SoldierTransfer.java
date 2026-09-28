package uz.askar.education.soldiers;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.Subdivision;

/** Askar boshqa qismga/bo'linmaga o'tkazilgani tarixi (yig'ma jild u bilan birga ko'chadi). */
@Entity
@Table(name = "soldier_transfers")
@Getter
@Setter
@NoArgsConstructor
public class SoldierTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soldier_id")
    private Soldier soldier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_unit_id")
    private MilitaryUnit fromUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_unit_id")
    private MilitaryUnit toUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_subdivision_id")
    private Subdivision fromSubdivision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_subdivision_id")
    private Subdivision toSubdivision;

    private String reason;
    private LocalDateTime transferredAt;
    private String transferredBy;
}
