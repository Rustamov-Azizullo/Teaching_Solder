package uz.askar.education.facilities;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @Query("""
            select f from Facility f
            where (:districtId is null or f.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or f.militaryUnit.id = :unitId)
            order by f.militaryUnit.name, f.name
            """)
    List<Facility> findInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);
}
