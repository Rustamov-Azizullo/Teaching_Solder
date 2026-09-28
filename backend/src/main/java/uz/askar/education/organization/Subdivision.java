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

/** Harbiy qism tarkibidagi bo'linma (batalon → rota → vzvod ...): cheksiz chuqurlikdagi ierarxiya. */
@Entity
@Table(name = "subdivisions")
@Getter
@Setter
@NoArgsConstructor
public class Subdivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Subdivision parent;

    private String name;

    /** "Batalon / Rota / Vzvod" ko'rinishidagi to'liq yo'l. */
    public String path() {
        return parent == null ? name : parent.path() + " / " + name;
    }
}
