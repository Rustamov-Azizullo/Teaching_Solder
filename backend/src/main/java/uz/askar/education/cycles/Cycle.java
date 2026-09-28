package uz.askar.education.cycles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Yillik sikl (chaqiruv yili): ochiq/yopiq. */
@Entity
@Table(name = "cycles")
@Getter
@Setter
@NoArgsConstructor
public class Cycle {

    @Id
    @Column(name = "cycle_year")
    private Integer year;

    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    public boolean isOpen() {
        return "OPEN".equals(status);
    }
}
