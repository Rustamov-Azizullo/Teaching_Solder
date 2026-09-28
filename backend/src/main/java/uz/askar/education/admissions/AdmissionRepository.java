package uz.askar.education.admissions;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.askar.education.soldiers.Soldier;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    Optional<Admission> findBySoldierIdAndCycleYear(Long soldierId, int cycleYear);

    List<Admission> findByCycleYear(int cycleYear);

    /** OTMga topshirmoqchi bo'lgan (yakunlangan anketa) yoki OTM tayyorlov guruhi a'zosi bo'lgan askarlar. */
    @Query("""
            select distinct s from Soldier s
            where (:districtId is null or s.militaryUnit.militaryDistrict.id = :districtId)
              and (:unitId is null or s.militaryUnit.id = :unitId)
              and (exists (select 1 from Questionnaire q join q.futurePlans p
                           where q.soldier = s and q.status = 'FINALIZED' and p.code = 'OTM_ADMISSION')
                or exists (select 1 from StudyGroup g join g.soldiers gs
                           where gs = s and g.type = 'OTM_PREP'))
            order by s.fullName
            """)
    List<Soldier> findCandidates(@Param("districtId") Long districtId, @Param("unitId") Long unitId);
}
