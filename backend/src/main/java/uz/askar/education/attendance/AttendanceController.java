package uz.askar.education.attendance;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.attendance.AttendanceDtos.AttendanceRequest;
import uz.askar.education.attendance.AttendanceDtos.AttendanceSheet;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/lessons/{lessonId}/attendance")
@RequiredArgsConstructor
@Tag(name = "Davomat", description = "M8: kunlik davomat (guruh kattasi tomonidan kiritiladi)")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize(Access.GROUP_READ)
    public AttendanceSheet sheet(@PathVariable Long lessonId) {
        return attendanceService.sheet(lessonId);
    }

    @PutMapping
    @PreAuthorize(Access.ATTENDANCE_WRITE)
    public AttendanceSheet record(@PathVariable Long lessonId, @Valid @RequestBody AttendanceRequest request) {
        return attendanceService.record(lessonId, request);
    }
}
