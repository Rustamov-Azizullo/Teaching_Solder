package uz.askar.education.admissions;

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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.soldiers.Soldier;

/** OTMga qabul jarayoni: BMBA ro'yxatdan o'tish, test, qabul, onlayn o'qish (TT M10). */
@Entity
@Table(name = "admissions")
@Getter
@Setter
@NoArgsConstructor
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soldier_id")
    private Soldier soldier;

    private int cycleYear;
    private boolean bmbaRegistered;
    private boolean benefitsUploaded;
    private boolean testParticipated;
    private Double testScore;
    private boolean admitted;
    private String university;
    private String studyDirection;

    @Enumerated(EnumType.STRING)
    private StudyForm studyForm;

    @Enumerated(EnumType.STRING)
    private OnlineStatus onlineStatus = OnlineStatus.NONE;

    private LocalDateTime bmbaSyncedAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
