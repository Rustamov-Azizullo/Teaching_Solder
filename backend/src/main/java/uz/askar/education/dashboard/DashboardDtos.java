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

    public record UnitSoldiers(Long id, String name, long soldiers) {
    }

    /** Harbiy okrug kesimida askarlar soni va okrug ichidagi harbiy qismlar taqsimoti. */
    public record DistrictSoldiers(Long id, String name, long soldiers, List<UnitSoldiers> units) {
    }

    /** Kasb bo'yicha o'qiyotgan askarlar soni va ularning okrug/harbiy qism kesimidagi taqsimoti. */
    public record ProfessionRow(String profession, long soldiers, List<DistrictSoldiers> districts) {
    }

    /** Viloyat (yoki Toshkent shahri, Qoraqalpog'iston Respublikasi) bo'yicha harbiy xizmat o'tayotgan askarlar soni. */
    public record RegionRow(String name, long soldiers) {
    }

    /** Okrugda muayyan kasb bo'yicha, muayyan muassasa bilan o'qiyotgan askarlar soni. */
    public record ProgramRow(String profession, String institution, long soldiers) {
    }

    /**
     * Okrug kesimi: jami askarlar, kasb kursi va OTM tayyorlovdagilar, kasblar va muassasalar soni
     * ({@code programs} — kasb + muassasa bo'yicha tafsilot).
     */
    public record DistrictRow(Long id, String name, long soldiers, long vocational, long otm, long professions,
                              long institutions, List<ProgramRow> programs) {
    }

    /**
     * Yagona dashboard ko'rsatkichlari: umumiy raqamlar, kasblar kesimi, okruglar va viloyatlar kesimi.
     * {@code unassigned} — hech bir kursga biriktirilmagan askarlar.
     */
    public record Overview(long totalSoldiers, long vocationalStudying, long otmPreparing, long unassigned,
                           long certified, long higherCompleted, long higherIncomplete,
                           List<ProfessionRow> professions, List<DistrictRow> districts, List<RegionRow> regions) {
    }

    public record GeographyBlock(long totalSoldiers, List<DistrictSoldiers> districts) {
    }
}
