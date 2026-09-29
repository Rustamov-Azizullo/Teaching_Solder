package uz.askar.education.groups;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    @Query("""
            select g from StudyGroup g
            where (:type is null or g.type = :type)
              and (:districtId is null or g.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or g.militaryUnit.id = :unitId)
            order by g.name
            """)
    List<StudyGroup> search(@Param("type") GroupType type, @Param("districtId") Long districtId,
                            @Param("unitId") Long unitId);

    boolean existsByInstitutionIdAndMilitaryUnitId(Long institutionId, Long unitId);

    @Query("""
            select count(g) > 0 from StudyGroup g join g.teachers t
            where g.militaryUnit.id = :unitId and t.institution.id = :institutionId
            """)
    boolean existsByUnitAndTeacherInstitution(@Param("unitId") Long unitId, @Param("institutionId") Long institutionId);

    List<StudyGroup> findByLeaderId(Long leaderId);

    List<StudyGroup> findByLeaderIsNotNull();

    @Query("select g from StudyGroup g join g.soldiers s where s.id = :soldierId")
    List<StudyGroup> findBySoldierId(@Param("soldierId") Long soldierId);

    @Query("""
            select count(g) from StudyGroup g join g.soldiers s
            where s.id = :soldierId and g.id <> :excludedGroupId
            """)
    long countOtherMemberships(@Param("soldierId") Long soldierId, @Param("excludedGroupId") Long excludedGroupId);

    @Query("""
            select s.id from StudyGroup g join g.soldiers s
            where g.militaryUnit.id = :unitId and g.id <> :excludedGroupId
            """)
    List<Long> findSoldierIdsInOtherGroups(@Param("unitId") Long unitId, @Param("excludedGroupId") Long excludedGroupId);
}
