package uz.askar.education.dictionaries;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Qism uchun faollashtirilgan kasb yo'nalishi. */
@Entity
@Table(name = "unit_directions")
@Getter
@NoArgsConstructor
public class UnitDirection {

    @EmbeddedId
    private Key id;

    public UnitDirection(Long unitId, Long directionId) {
        this.id = new Key(unitId, directionId);
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {

        @Column(name = "military_unit_id")
        private Long unitId;

        @Column(name = "direction_id")
        private Long directionId;

        public Key(Long unitId, Long directionId) {
            this.unitId = unitId;
            this.directionId = directionId;
        }
    }
}
