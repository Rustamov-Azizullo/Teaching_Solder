package uz.askar.education.locations;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByParentId(Long parentId);

    Optional<Location> findFirstByLevel(LocationLevel level);

    Optional<Location> findByMilitaryDistrictId(Long militaryDistrictId);

    Optional<Location> findByMilitaryUnitId(Long militaryUnitId);

    /** Okrug doirasidagi hududlar (okrugning o'zi va uning qismlari); {@code districtId = null} bo'lsa — barchasi. */
    @Query("""
            select l from Location l
            left join l.parent p
            left join l.militaryDistrict ownDistrict
            left join p.militaryDistrict parentDistrict
            where :districtId is null
               or ownDistrict.id = :districtId
               or parentDistrict.id = :districtId
            order by l.level, l.name
            """)
    List<Location> findInDistrictScope(@Param("districtId") Long districtId);
}
