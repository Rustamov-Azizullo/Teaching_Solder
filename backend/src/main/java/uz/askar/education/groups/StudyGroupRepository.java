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
              and (:leaderId is null or g.leader.id = :leaderId)
            order by g.name
            """)
    List<StudyGroup> search(@Param("type") GroupType type, @Param("districtId") Long districtId,
                            @Param("unitId") Long unitId, @Param("leaderId") Long leaderId);

    boolean existsByLeaderId(Long leaderId);

    @Query("select g from StudyGroup g join g.soldiers s where s.id = :soldierId")
    List<StudyGroup> findBySoldierId(@Param("soldierId") Long soldierId);

    @Query("""
            select count(g) from StudyGroup g join g.soldiers s
            where s.id = :soldierId and g.type = :type and g.cycleYear = :cycleYear and g.id <> :excludedGroupId
            """)
    long countOtherMemberships(@Param("soldierId") Long soldierId, @Param("type") GroupType type,
                               @Param("cycleYear") int cycleYear, @Param("excludedGroupId") Long excludedGroupId);
}
