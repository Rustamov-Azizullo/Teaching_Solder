package uz.askar.education.dashboard;

import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record CountItem(String label, long count) {
    }

    public record SubjectNeed(String subject, long specialtyCount, long mandatoryCount) {
    }

    public record CompletionRow(Long unitId, String unitName, long soldiers, long finalized, double percent) {
    }

    public record EducationStats(List<CountItem> educationLevels, List<CountItem> certificatesAndAwards) {
    }

    public record SurveyBlock(long totalSoldiers, long finalizedQuestionnaires, List<CompletionRow> completion,
                              List<CountItem> interests, List<CountItem> futurePlans,
                              List<SubjectNeed> subjectNeeds, EducationStats education) {
    }

    /** Muassasaning harbiy qism bilan biriktirilishi/shartnomasi. */
    public record UnitContract(Long unitId, String unitName, String status, String contractNo, LocalDate contractDate) {
    }

    public record InstitutionContracts(Long id, String name, String type, List<UnitContract> units) {
    }

    /** Hudud kesimida: texnikum/muassasalar va ularning harbiy qismlar bilan shartnomalari. */
    public record RegionInstitutions(String region, long soldiers, long institutionCount, long contractCount,
                                     List<InstitutionContracts> institutions) {
    }

    public record UnitSoldiers(Long id, String name, long soldiers) {
    }

    /** Harbiy okrug kesimida askarlar soni va okrug ichidagi harbiy qismlar taqsimoti. */
    public record DistrictSoldiers(Long id, String name, long soldiers, List<UnitSoldiers> units) {
    }

    public record GeographyBlock(long totalSoldiers, List<DistrictSoldiers> districts,
                                 List<RegionInstitutions> regions) {
    }
}
