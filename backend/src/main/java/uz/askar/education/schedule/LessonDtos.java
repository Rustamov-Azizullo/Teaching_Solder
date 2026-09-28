package uz.askar.education.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import uz.askar.education.groups.GroupType;

public final class LessonDtos {

    private LessonDtos() {
    }

    public record LessonRequest(
            @NotNull LocalDate lessonDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer academicHours,
            @Size(max = 255) String topic,
            @NotNull LessonKind kind,
            @Size(max = 255) String changeReason) {
    }

    public record GenerateLessonsRequest(@NotNull LocalDate from, @NotNull LocalDate to, @NotNull LessonKind kind) {
    }

    public record CancelRequest(@NotBlank @Size(max = 255) String reason) {
    }

    public record LessonDto(Long id, Long groupId, String groupName, GroupType groupType, LocalDate lessonDate,
                            LocalTime startTime, LocalTime endTime, int academicHours, String topic,
                            LessonKind kind, LessonStatus status, String changeReason, Boolean teacherPresent,
                            boolean attendanceRecorded) {
    }
}
