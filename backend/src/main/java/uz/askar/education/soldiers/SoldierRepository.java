package uz.askar.education.soldiers;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SoldierRepository extends JpaRepository<Soldier, Long> {

    Optional<Soldier> findByPinfl(String pinfl);

    boolean existsByPinfl(String pinfl);

    @Query("""
            select s from Soldier s
            where (lower(s.fullName) like lower(concat('%', :query, '%')) or s.pinfl like concat('%', :query, '%'))
              and (:districtId is null or s.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or s.militaryUnit.id = :unitId)
              and (:useSubdivision = false or s.subdivision.id in :subdivisionIds)
            """)
    Page<Soldier> search(@Param("query") String query, @Param("districtId") Long districtId,
                         @Param("unitId") Long unitId,
                         @Param("useSubdivision") boolean useSubdivision,
                         @Param("subdivisionIds") java.util.Collection<Long> subdivisionIds, Pageable pageable);

    @Query("""
            select count(s) from Soldier s
            where (:districtId is null or s.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or s.militaryUnit.id = :unitId)
            """)
    long countInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);

    @Query("""
            select s from Soldier s
            where (:districtId is null or s.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or s.militaryUnit.id = :unitId)
            """)
    java.util.List<Soldier> findAllInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);

    @Query("select s from Soldier s where s.serviceEndDate = :date")
    java.util.List<Soldier> findServiceEndedOn(@Param("date") java.time.LocalDate date);

    @Query("select s from Soldier s where s.serviceEndDate < :cutoff")
    java.util.List<Soldier> findServiceEndedBefore(@Param("cutoff") java.time.LocalDate cutoff);
}
