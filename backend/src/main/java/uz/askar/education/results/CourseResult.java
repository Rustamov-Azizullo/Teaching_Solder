package uz.askar.education.results;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.soldiers.Soldier;

/** Askarning kurs natijasi: o'qidi → imtihondan o'tdi → sertifikat oldi / o'qishni tugatmadi (TT M9). */
@Entity
@Table(name = "course_results")
@Getter
@Setter
@NoArgsConstructor
public class CourseResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soldier_id")
    private Soldier soldier;

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    private Integer examGrade;
    private String dropReason;
    private String certificateNo;
    private LocalDate certificateDate;
    private String certificateIssuer;
    private String recordedBy;
    private LocalDateTime updatedAt;
}
