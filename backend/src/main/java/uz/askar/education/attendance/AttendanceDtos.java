package uz.askar.education.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import uz.askar.education.schedule.LessonDtos.LessonDto;

public final class AttendanceDtos {

    private AttendanceDtos() {
    }

    public record AttendanceEntryInput(@NotNull Long soldierId, @NotNull AttendanceStatus status,
                                       AbsenceReason reason) {
    }

    public record AttendanceRequest(
            @NotNull Boolean teacherPresent,
            @Size(max = 255) String topic,
            @NotEmpty @Valid List<AttendanceEntryInput> entries) {
    }

    public record RosterEntry(Long soldierId, String fullName, AttendanceStatus status, AbsenceReason reason) {
    }

    public record AttendanceSheet(LessonDto lesson, List<RosterEntry> roster, boolean editable,
                                  String readOnlyReason) {
    }
}
