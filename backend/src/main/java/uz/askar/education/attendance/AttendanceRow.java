package uz.askar.education.attendance;

import java.time.LocalDate;

/** Davomat qatori — dashboard agregatlari uchun yengil proyeksiya. */
public record AttendanceRow(Long lessonId, LocalDate lessonDate, AttendanceStatus status, AbsenceReason reason, Long groupId,
                            String groupName, Long unitId, String unitName, Long districtId, String districtName,
                            int academicHours) {

    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT;
    }
}
