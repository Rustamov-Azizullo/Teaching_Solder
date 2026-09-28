package uz.askar.education.schedule;

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
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.groups.StudyGroup;

/** Mashg'ulot: dars jadvali (M7) va davomat (M8) uchun umumiy yozuv. */
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int academicHours;
    private String topic;

    @Enumerated(EnumType.STRING)
    private LessonKind kind;

    @Enumerated(EnumType.STRING)
    private LessonStatus status = LessonStatus.PLANNED;

    private String changeReason;
    private Boolean teacherPresent;
    private boolean attendanceRecorded;
    private LocalDateTime attendanceRecordedAt;
}
