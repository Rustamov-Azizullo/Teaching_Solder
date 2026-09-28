package uz.askar.education.dictionaries;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnitDirectionRepository extends JpaRepository<UnitDirection, UnitDirection.Key> {

    @Query("select u.id.directionId from UnitDirection u where u.id.unitId = :unitId")
    List<Long> findDirectionIds(@Param("unitId") Long unitId);

    @Modifying
    @Query("delete from UnitDirection u where u.id.unitId = :unitId")
    void deleteByUnitId(@Param("unitId") Long unitId);
}
