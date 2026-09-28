package uz.askar.education.surveys;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    Optional<Questionnaire> findBySoldierIdAndCycleYear(Long soldierId, int cycleYear);

    @Query("""
            select q from Questionnaire q
            where q.cycleYear = :cycleYear and q.status = 'FINALIZED'
              and (:districtId is null or q.soldier.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or q.soldier.militaryUnit.id = :unitId)
            """)
    List<Questionnaire> findFinalizedInScope(@Param("cycleYear") int cycleYear,
                                             @Param("districtId") Long districtId, @Param("unitId") Long unitId);
}
