package uz.askar.education.schedule;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByGroupIdAndLessonDateBetweenOrderByLessonDateAscStartTimeAsc(Long groupId, LocalDate from,
                                                                                    LocalDate to);

    boolean existsByGroupIdAndLessonDate(Long groupId, LocalDate lessonDate);

    @Query("""
            select l from Lesson l
            where l.lessonDate = :date
              and (:districtId is null or l.group.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or l.group.militaryUnit.id = :unitId)
              and (:leaderId is null or l.group.leader.id = :leaderId)
            order by l.startTime, l.group.name
            """)
    List<Lesson> findByDateInScope(@Param("date") LocalDate date, @Param("districtId") Long districtId,
                                   @Param("unitId") Long unitId, @Param("leaderId") Long leaderId);
}
