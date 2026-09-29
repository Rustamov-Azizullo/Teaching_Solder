package uz.askar.education.groups;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    List<Teacher> findAllByOrderByFullNameAsc();

    @Modifying
    @Query(value = "delete from group_teachers where teacher_id = :teacherId", nativeQuery = true)
    void detachFromGroups(@Param("teacherId") Long teacherId);
}
