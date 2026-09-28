package uz.askar.education.dashboard;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import uz.askar.education.attendance.AbsenceReason;
import uz.askar.education.attendance.AttendanceRow;
import uz.askar.education.dashboard.DashboardDtos.Breakdown;
import uz.askar.education.dashboard.DashboardDtos.BreakdownRow;
import uz.askar.education.dashboard.DashboardDtos.DailyPoint;
import uz.askar.education.dashboard.DashboardDtos.ReasonSlice;
import uz.askar.education.dashboard.DashboardDtos.WeeklyPoint;

/** Davomat qatorlaridan diagramma ma'lumotlarini hisoblaydigan sof funksiyalar (TT M12). */
final class AttendanceAggregator {

    private static final double PERCENT = 100.0;
    private static final double ROUNDING_FACTOR = 10.0;

    private AttendanceAggregator() {
    }

    static List<DailyPoint> daily(List<AttendanceRow> rows) {
        Map<LocalDate, List<AttendanceRow>> byDate = rows.stream()
                .collect(Collectors.groupingBy(AttendanceRow::lessonDate, TreeMap::new, Collectors.toList()));
        return byDate.entrySet().stream()
                .map(entry -> new DailyPoint(entry.getKey(), percent(entry.getValue()))).toList();
    }

    static List<WeeklyPoint> weekly(List<AttendanceRow> rows) {
        Map<LocalDate, List<AttendanceRow>> byWeek = rows.stream().collect(Collectors.groupingBy(
                row -> row.lessonDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                TreeMap::new, Collectors.toList()));
        return byWeek.entrySet().stream()
                .map(entry -> new WeeklyPoint(entry.getKey(), percent(entry.getValue()), heldHours(entry.getValue())))
                .toList();
    }

    static Breakdown breakdown(List<AttendanceRow> rows, Long districtId, Long unitId) {
        if (unitId != null) {
            return breakdownBy("GROUP", rows, AttendanceRow::groupId, AttendanceRow::groupName);
        }
        if (districtId != null) {
            return breakdownBy("UNIT", rows, AttendanceRow::unitId, AttendanceRow::unitName);
        }
        return breakdownBy("DISTRICT", rows, AttendanceRow::districtId, AttendanceRow::districtName);
    }

    static List<ReasonSlice> reasons(List<AttendanceRow> rows) {
        Map<AbsenceReason, Long> counts = rows.stream()
                .filter(row -> !row.isPresent() && row.reason() != null)
                .collect(Collectors.groupingBy(AttendanceRow::reason, Collectors.counting()));
        return Arrays.stream(AbsenceReason.values())
                .map(reason -> new ReasonSlice(reason.name(), reasonLabel(reason), counts.getOrDefault(reason, 0L)))
                .toList();
    }

    static double percent(List<AttendanceRow> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        long present = rows.stream().filter(AttendanceRow::isPresent).count();
        return Math.round(present * PERCENT * ROUNDING_FACTOR / rows.size()) / ROUNDING_FACTOR;
    }

    /** Har bir mashg'ulot soati bir marta hisoblanadi (davomat qatorlari soni bo'yicha emas). */
    private static int heldHours(List<AttendanceRow> rows) {
        return rows.stream().collect(Collectors.toMap(AttendanceRow::lessonId, AttendanceRow::academicHours,
                (first, second) -> first)).values().stream().mapToInt(Integer::intValue).sum();
    }

    private static Breakdown breakdownBy(String level, List<AttendanceRow> rows,
                                         Function<AttendanceRow, Long> idOf, Function<AttendanceRow, String> labelOf) {
        Map<Long, List<AttendanceRow>> grouped = rows.stream()
                .collect(Collectors.groupingBy(idOf, Collectors.toList()));
        List<BreakdownRow> result = grouped.entrySet().stream()
                .map(entry -> {
                    List<AttendanceRow> group = entry.getValue();
                    return new BreakdownRow(entry.getKey(), labelOf.apply(group.get(0)), percent(group),
                            group.stream().filter(AttendanceRow::isPresent).count(), group.size());
                })
                .sorted(Comparator.comparingDouble(BreakdownRow::percent).reversed())
                .toList();
        return new Breakdown(level, result);
    }

    private static String reasonLabel(AbsenceReason reason) {
        return switch (reason) {
            case DUTY -> "Navbatchilik";
            case ILLNESS -> "Kasallik";
            case SERVICE_TASK -> "Xizmat vazifasi";
            case NO_REASON -> "Sababsiz";
        };
    }
}
