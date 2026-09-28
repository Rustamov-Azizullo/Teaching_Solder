package uz.askar.education.organization;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubdivisionRepository extends JpaRepository<Subdivision, Long> {

    List<Subdivision> findByMilitaryUnitIdOrderByNameAsc(Long unitId);

    boolean existsByParentId(Long parentId);

    @Query("select count(s) from Soldier s where s.subdivision.id = :subdivisionId")
    long countSoldiers(@Param("subdivisionId") Long subdivisionId);
}
