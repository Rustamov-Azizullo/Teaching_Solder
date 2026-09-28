package uz.askar.education.attendance;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.askar.education.groups.GroupType;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByLessonId(Long lessonId);

    /** Dashboard uchun: davomat qatorlari (vakolat doirasi va davr bo'yicha). */
    @Query("""
            select new uz.askar.education.attendance.AttendanceRow(
                l.id, l.lessonDate, a.status, a.reason, g.id, g.name, u.id, u.name, d.id, d.name, l.academicHours)
            from Attendance a
              join a.lesson l
              join l.group g
              join g.militaryUnit u
              join u.militaryDistrict d
            where g.type = :type
              and l.lessonDate between :from and :to
              and (:districtId is null or d.id = :districtId)
              and (:unitId is null or u.id = :unitId)
            """)
    List<AttendanceRow> findRows(@Param("type") GroupType type, @Param("from") LocalDate from,
                                 @Param("to") LocalDate to, @Param("districtId") Long districtId,
                                 @Param("unitId") Long unitId);
}
