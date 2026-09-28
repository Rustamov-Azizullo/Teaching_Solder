package uz.askar.education.dashboard;

import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record DailyPoint(LocalDate date, double percent) {
    }

    public record WeeklyPoint(LocalDate weekStart, double averagePercent, int academicHours) {
    }

    public record BreakdownRow(Long id, String label, double percent, long present, long total) {
    }

    /** Davomat kesimi: level = DISTRICT | UNIT | GROUP — drill-down keyingi darajasini ko'rsatadi. */
    public record Breakdown(String level, List<BreakdownRow> rows) {
    }

    public record ReasonSlice(String reason, String label, long count) {
    }

    public record AttendanceSummary(long totalSoldiers, long totalGroups, double todayPercent,
                                    long groupsWithoutAttendanceToday) {
    }

    public record AttendanceBlock(AttendanceSummary summary, List<DailyPoint> daily, List<WeeklyPoint> weekly,
                                  Breakdown breakdown, List<ReasonSlice> absenceReasons) {
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
