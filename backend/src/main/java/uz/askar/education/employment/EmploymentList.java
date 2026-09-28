package uz.askar.education.employment;

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
import uz.askar.education.organization.Region;

/** Qaysi ro'yxat qachon, qaysi idoraga tayyorlangani qayd etiladi (TT M11). */
@Entity
@Table(name = "employment_lists")
@Getter
@Setter
@NoArgsConstructor
public class EmploymentList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    private String agency;
    private int soldierCount;
    private String format;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private int cycleYear;
}
