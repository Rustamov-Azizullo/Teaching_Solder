package uz.askar.education.organization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MilitaryUnitRepository extends JpaRepository<MilitaryUnit, Long> {

    java.util.Optional<MilitaryUnit> findByCode(String code);

    @Query("""
            select u from MilitaryUnit u
            where (:districtId is null or u.militaryDistrict.id = :districtId)
              and (:unitId is null or u.id = :unitId)
            order by u.name
            """)
    List<MilitaryUnit> findInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);
}
