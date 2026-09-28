package uz.askar.education.assignments;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("""
            select a from Assignment a
            where (:districtId is null or a.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or a.militaryUnit.id = :unitId)
            order by a.createdAt desc
            """)
    List<Assignment> findInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);
}
