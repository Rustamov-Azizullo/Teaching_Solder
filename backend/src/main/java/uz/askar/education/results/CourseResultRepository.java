package uz.askar.education.results;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.askar.education.groups.GroupType;

public interface CourseResultRepository extends JpaRepository<CourseResult, Long> {

    List<CourseResult> findByGroupId(Long groupId);

    @Query("""
            select r from CourseResult r
            where r.group.type = :type
              and (:districtId is null or r.group.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or r.group.militaryUnit.id = :unitId)
            """)
    List<CourseResult> findInScope(@Param("type") GroupType type, @Param("districtId") Long districtId,
                                   @Param("unitId") Long unitId);

    @Query("""
            select r from CourseResult r
            where r.status = 'CERTIFIED'
              and (:districtId is null or r.group.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or r.group.militaryUnit.id = :unitId)
            """)
    List<CourseResult> findCertifiedInScope(@Param("districtId") Long districtId, @Param("unitId") Long unitId);

    /** Guruhdan chiqarilgan askarlarning kurs natijalarini o'chiradi. */
    @Modifying(flushAutomatically = true)
    @Query("delete from CourseResult r where r.group.id = :groupId and r.soldier.id in :soldierIds")
    void deleteByGroupIdAndSoldierIdIn(@Param("groupId") Long groupId, @Param("soldierIds") Collection<Long> soldierIds);
}
