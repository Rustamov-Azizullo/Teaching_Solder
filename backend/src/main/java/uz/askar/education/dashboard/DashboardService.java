package uz.askar.education.dashboard;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.dashboard.DashboardDtos.SurveyBlock;
import uz.askar.education.groups.GroupType;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierRepository;
import uz.askar.education.surveys.Questionnaire;
import uz.askar.education.surveys.QuestionnaireRepository;

/**
 * Dashboard agregatlari. Barcha so'rovlar vakolat doirasi bilan cheklanadi: okrug/qism filtrlari
 * foydalanuvchi vakolatidan olinadi, so'rovdagi filtr esa faqat uni toraytirishi mumkin.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SoldierRepository soldiers;
    private final QuestionnaireRepository questionnaires;
    private final uz.askar.education.results.CourseResultRepository courseResults;
    private final uz.askar.education.admissions.AdmissionRepository admissions;
    private final uz.askar.education.groups.StudyGroupRepository groups;
    private final uz.askar.education.organization.RegionRepository regions;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public SurveyBlock surveyBlock(Long districtId, Long unitId) {
        AccessScope scope = currentUser.scope();
        Long effectiveDistrict = scope.districtFilter() != null ? scope.districtFilter() : districtId;
        Long effectiveUnit = scope.unitFilter() != null ? scope.unitFilter() : unitId;

        List<Soldier> scopedSoldiers = soldiers.findAllInScope(effectiveDistrict, effectiveUnit);
        List<Questionnaire> finalized = questionnaires.findFinalizedInScope(
                LocalDate.now().getYear(), effectiveDistrict, effectiveUnit);
        return new SurveyBlock(scopedSoldiers.size(), finalized.size(),
                SurveyAggregator.completion(scopedSoldiers, finalized),
                SurveyAggregator.interests(finalized),
                SurveyAggregator.futurePlans(finalized),
                SurveyAggregator.subjectNeeds(finalized),
                SurveyAggregator.education(scopedSoldiers));
    }

    /** Yagona dashboard: umumiy raqamlar, kasblar kesimi va hududlar bo'yicha tanlovlar. */
    @Transactional(readOnly = true)
    public DashboardDtos.Overview overview(Long districtId, Long unitId) {
        AccessScope scope = currentUser.scope();
        Long d = scope.districtFilter() != null ? scope.districtFilter() : districtId;
        Long u = scope.unitFilter() != null ? scope.unitFilter() : unitId;
        int year = LocalDate.now().getYear();

        List<Soldier> scopedSoldiers = soldiers.findAllInScope(d, u);
        List<StudyGroup> vocationalGroups = currentGroups(GroupType.VOCATIONAL, d, u, year);
        List<StudyGroup> otmGroups = currentGroups(GroupType.OTM_PREP, d, u, year);
        Set<Soldier> vocational = OverviewAggregator.soldiersOf(vocationalGroups);
        Set<Soldier> otm = OverviewAggregator.soldiersOf(otmGroups);
        Set<Soldier> assigned = new HashSet<>(vocational);
        assigned.addAll(otm);

        return new DashboardDtos.Overview(scopedSoldiers.size(), vocational.size(), otm.size(),
                scopedSoldiers.stream().filter(s -> !assigned.contains(s)).count(),
                OverviewAggregator.countCertified(scopedSoldiers),
                OverviewAggregator.countHigher(scopedSoldiers, true),
                OverviewAggregator.countHigher(scopedSoldiers, false),
                OverviewAggregator.professions(vocationalGroups),
                DistrictBreakdownAggregator.districts(scopedSoldiers, vocationalGroups, otmGroups),
                OverviewAggregator.regions(regions.findAllByOrderByNameAsc(), scopedSoldiers));
    }

    private List<StudyGroup> currentGroups(GroupType type, Long districtId, Long unitId, int year) {
        return groups.search(type, districtId, unitId).stream().filter(group -> group.getCycleYear() == year).toList();
    }

    /** Kurs natijalari diagrammasi: o'qiganlar, imtihondan o'tganlar, sertifikat olganlar (TT M12, 1-blok). */
    @Transactional(readOnly = true)
    public List<DashboardDtos.CountItem> courseResults(Long districtId, Long unitId) {
        AccessScope scope = currentUser.scope();
        Long d = scope.districtFilter() != null ? scope.districtFilter() : districtId;
        Long u = scope.unitFilter() != null ? scope.unitFilter() : unitId;
        var rows = courseResults.findInScope(GroupType.VOCATIONAL, d, u);
        java.util.function.Function<uz.askar.education.results.CourseStatus, Long> count =
                status -> rows.stream().filter(r -> r.getStatus() == status).count();
        return List.of(
                new DashboardDtos.CountItem("O'qidi", rows.stream().filter(r -> r.getStatus() != uz.askar.education.results.CourseStatus.DROPPED).count()),
                new DashboardDtos.CountItem("Imtihondan o'tdi", count.apply(uz.askar.education.results.CourseStatus.EXAM_PASSED)
                        + count.apply(uz.askar.education.results.CourseStatus.CERTIFIED)),
                new DashboardDtos.CountItem("Sertifikat oldi", count.apply(uz.askar.education.results.CourseStatus.CERTIFIED)),
                new DashboardDtos.CountItem("Tugatmadi", count.apply(uz.askar.education.results.CourseStatus.DROPPED)));
    }

    /** Askarlarning okrug/qism kesimidagi taqsimoti. */
    @Transactional(readOnly = true)
    public DashboardDtos.GeographyBlock geography(Long districtId, Long unitId) {
        AccessScope scope = currentUser.scope();
        Long effectiveDistrict = scope.districtFilter() != null ? scope.districtFilter() : districtId;
        Long effectiveUnit = scope.unitFilter() != null ? scope.unitFilter() : unitId;
        List<Soldier> scopedSoldiers = soldiers.findAllInScope(effectiveDistrict, effectiveUnit);
        return new DashboardDtos.GeographyBlock(scopedSoldiers.size(), GeographyAggregator.districts(scopedSoldiers));
    }

    /** OTMga tayyorlanuvchi nomzod askarlarning okrug va harbiy qism kesimidagi taqsimoti. */
    @Transactional(readOnly = true)
    public List<DashboardDtos.DistrictSoldiers> otmCandidatesByDistrict(Long districtId, Long unitId) {
        AccessScope scope = currentUser.scope();
        Long effectiveDistrict = scope.districtFilter() != null ? scope.districtFilter() : districtId;
        Long effectiveUnit = scope.unitFilter() != null ? scope.unitFilter() : unitId;
        return GeographyAggregator.districts(admissions.findCandidates(effectiveDistrict, effectiveUnit));
    }
}
