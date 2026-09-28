package uz.askar.education.groups;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query("""
            select t from Teacher t
            where (:districtId is null or t.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or t.militaryUnit.id = :unitId)
            order by t.fullName
            """)
    List<Teacher> findInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);
}
