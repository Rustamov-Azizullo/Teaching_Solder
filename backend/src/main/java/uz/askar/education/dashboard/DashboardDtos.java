package uz.askar.education.dashboard;

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
}
