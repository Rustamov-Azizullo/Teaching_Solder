package uz.askar.education.reports;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.admissions.AdmissionService;
import uz.askar.education.attendance.AttendanceRepository;
import uz.askar.education.attendance.AttendanceRow;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.ForbiddenException;
import uz.askar.education.export.ExportFormat;
import uz.askar.education.export.TableData;
import uz.askar.education.export.TableExporter;
import uz.askar.education.groups.GroupType;
import uz.askar.education.groups.StudyGroupRepository;
import uz.askar.education.results.CourseResult;
import uz.askar.education.results.CourseResultRepository;
import uz.askar.education.results.CourseStatus;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Role;
import uz.askar.education.soldiers.SoldierRepository;
import uz.askar.education.surveys.QuestionnaireRepository;

/**
 * Hisobotlar (TT M12): yagona uslubda — sarlavha, davr, vakolat doirasi, tuzilgan sana va tuzuvchi bilan.
 * Barcha hisobotlar vakolat doirasi bilan cheklanadi va eksport audit jurnaliga yoziladi.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final double PERCENT = 100.0;
    private static final double ROUND = 10.0;

    public record ReportFile(String fileName, byte[] content, ExportFormat format) {
    }

    private final AttendanceRepository attendances;
    private final CourseResultRepository results;
    private final AdmissionService admissions;
    private final StudyGroupRepository groups;
    private final SoldierRepository soldiers;
    private final QuestionnaireRepository questionnaires;
    private final TableExporter exporter;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public ReportFile generate(ReportType type, GroupType groupType, LocalDate from, LocalDate to, ExportFormat format) {
        requireAllowed(type, groupType);
        TableData data = switch (type) {
            case ATTENDANCE_DAILY -> attendanceDaily(groupType, to);
            case ATTENDANCE_WEEKLY -> attendanceWeekly(groupType, from, to);
            case COURSE_COMPLETION -> courseCompletion();
            case OTM_ADMISSIONS -> otmAdmissions();
            case YEARLY_SUMMARY -> yearlySummary(to.getYear());
        };
        audit.record("EXPORT", "Report", type.name(), format + ", qatorlar: " + data.rows().size());
        return new ReportFile(type.name().toLowerCase() + "-" + LocalDate.now() + "." + format.extension(),
                exporter.export(data, format), format);
    }

    private TableData attendanceDaily(GroupType type, LocalDate date) {
        var scope = currentUser.scope();
        List<AttendanceRow> rows = attendances.findRows(type, date, date, scope.districtFilter(), scope.unitFilter());
        Map<Long, List<AttendanceRow>> byGroup = rows.stream().collect(Collectors.groupingBy(AttendanceRow::groupId));
        List<List<String>> table = byGroup.values().stream().map(list -> {
            long present = list.stream().filter(AttendanceRow::isPresent).count();
            AttendanceRow first = list.get(0);
            return List.of(first.districtName(), first.unitName(), first.groupName(), String.valueOf(present),
                    String.valueOf(list.size() - present), percent(present, list.size()));
        }).sorted(Comparator.comparing((List<String> r) -> r.get(1)).thenComparing(r -> r.get(2))).toList();
        return new TableData("Kunlik davomat hisoboti — " + typeLabel(type),
                meta("Sana: " + date), List.of("Okrug", "Qism", "Guruh", "Keldi", "Kelmadi", "Davomat %"), table);
    }

    private TableData attendanceWeekly(GroupType type, LocalDate from, LocalDate to) {
        var scope = currentUser.scope();
        List<AttendanceRow> rows = attendances.findRows(type, from, to, scope.districtFilter(), scope.unitFilter());
        Map<LocalDate, List<AttendanceRow>> byWeek = rows.stream().collect(Collectors.groupingBy(
                r -> r.lessonDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), TreeMap::new,
                Collectors.toList()));
        List<List<String>> table = new ArrayList<>();
        byWeek.forEach((week, list) -> {
            long present = list.stream().filter(AttendanceRow::isPresent).count();
            int hours = list.stream().collect(Collectors.toMap(AttendanceRow::lessonId, AttendanceRow::academicHours,
                    (a, b) -> a)).values().stream().mapToInt(Integer::intValue).sum();
            table.add(List.of(week.toString(), String.valueOf(list.size()), String.valueOf(present),
                    percent(present, list.size()), String.valueOf(hours)));
        });
        return new TableData("Haftalik umumlashma hisoboti — " + typeLabel(type), meta("Davr: " + from + " — " + to),
                List.of("Hafta boshi", "Belgilar soni", "Keldi", "Davomat %", "O'tilgan soat"), table);
    }

    private TableData courseCompletion() {
        var scope = currentUser.scope();
        Map<Long, List<CourseResult>> byGroup = results.findInScope(GroupType.VOCATIONAL, scope.districtFilter(),
                scope.unitFilter()).stream().collect(Collectors.groupingBy(r -> r.getGroup().getId()));
        List<List<String>> table = byGroup.values().stream().map(list -> {
            var group = list.get(0).getGroup();
            return List.of(group.getMilitaryUnit().getName(), group.getName(),
                    group.getProfession() == null ? "" : group.getProfession().getName(),
                    String.valueOf(count(list, CourseStatus.STUDIED)), String.valueOf(count(list, CourseStatus.EXAM_PASSED)),
                    String.valueOf(count(list, CourseStatus.CERTIFIED)), String.valueOf(count(list, CourseStatus.DROPPED)),
                    group.getCourseApprovedAt() == null ? "Yo'q" : "Ha");
        }).sorted(Comparator.comparing((List<String> r) -> r.get(0)).thenComparing(r -> r.get(1))).toList();
        return new TableData("Kurs yakuni hisoboti (HKTB uchun)", meta("Yillik sikl: " + LocalDate.now().getYear()),
                List.of("Qism", "Guruh", "Kasb", "O'qidi", "Imtihondan o'tdi", "Sertifikat oldi", "Tugatmadi",
                        "Qo'mondon tasdiqladi"), table);
    }

    private TableData otmAdmissions() {
        List<List<String>> table = admissions.list().stream().map(r -> List.of(r.fullName(), r.pinfl(), r.unitName(),
                yesNo(r.bmbaRegistered()), yesNo(r.testParticipated()), r.testScore() == null ? "" : r.testScore().toString(),
                yesNo(r.admitted()), nz(r.university()), nz(r.studyDirection()))).toList();
        return new TableData("OTMga qabul natijalari (HKTB uchun)", meta("Yillik sikl: " + LocalDate.now().getYear()),
                List.of("F.I.Sh.", "JShShIR", "Qism", "BMBA", "Test", "Ball", "Qabul", "OTM", "Yo'nalish"), table);
    }

    private TableData yearlySummary(int year) {
        var scope = currentUser.scope();
        Long d = scope.districtFilter();
        Long u = scope.unitFilter();
        var certified = results.findCertifiedInScope(d, u).size();
        List<List<String>> table = List.of(
                List.of("Jami askarlar", String.valueOf(soldiers.countInScope(d, u))),
                List.of("Kasb kurslari guruhlari", String.valueOf(groups.search(GroupType.VOCATIONAL, d, u, null).size())),
                List.of("OTM tayyorlov guruhlari", String.valueOf(groups.search(GroupType.OTM_PREP, d, u, null).size())),
                List.of("Yakunlangan anketalar", String.valueOf(questionnaires.findFinalizedInScope(year, d, u).size())),
                List.of("Sertifikat olganlar", String.valueOf(certified)),
                List.of("OTM nomzodlari", String.valueOf(admissions.list().size())),
                List.of("OTMga qabul qilinganlar", String.valueOf(admissions.list().stream().filter(r -> r.admitted()).count())));
        return new TableData("Vazirlik miqyosidagi yillik umumlashma", meta("Yil: " + year),
                List.of("Ko'rsatkich", "Qiymat"), table);
    }

    private void requireAllowed(ReportType type, GroupType groupType) {
        Role role = currentUser.role();
        boolean allowed = switch (type) {
            case ATTENDANCE_DAILY, ATTENDANCE_WEEKLY -> groupType == GroupType.VOCATIONAL
                    ? hasAny(role, Role.SYSTEM_ADMIN, Role.HKTB, Role.JTB, Role.DISTRICT_OFFICER, Role.UNIT_COMMANDER,
                            Role.UNIT_OPERATOR, Role.COMBAT_TRAINING_DEPT)
                    : hasAny(role, Role.SYSTEM_ADMIN, Role.HKTB, Role.TMIBB, Role.DISTRICT_OFFICER, Role.UNIT_COMMANDER,
                            Role.UNIT_OPERATOR, Role.EDUCATION_DEPT);
            case COURSE_COMPLETION -> hasAny(role, Role.SYSTEM_ADMIN, Role.HKTB, Role.DISTRICT_OFFICER,
                    Role.UNIT_COMMANDER, Role.UNIT_OPERATOR);
            case OTM_ADMISSIONS -> hasAny(role, Role.SYSTEM_ADMIN, Role.HKTB, Role.TMIBB, Role.DISTRICT_OFFICER,
                    Role.UNIT_COMMANDER, Role.UNIT_OPERATOR, Role.EDUCATION_DEPT);
            case YEARLY_SUMMARY -> hasAny(role, Role.SYSTEM_ADMIN, Role.HKTB, Role.DISTRICT_OFFICER, Role.UNIT_COMMANDER);
        };
        if (!allowed) {
            throw new ForbiddenException("Bu hisobot uchun vakolat yo'q");
        }
    }

    private boolean hasAny(Role role, Role... allowed) {
        for (Role candidate : allowed) {
            if (candidate == role) {
                return true;
            }
        }
        return false;
    }

    private List<String> meta(String period) {
        var scope = currentUser.scope();
        String area = switch (scope.level()) {
            case REPUBLIC -> "Respublika";
            case DISTRICT -> "Harbiy okrug #" + scope.districtId();
            case UNIT -> "Harbiy qism #" + scope.unitId();
        };
        return List.of(period, "Vakolat doirasi: " + area, "Tuzilgan sana: " + LocalDate.now(),
                "Tuzuvchi: " + currentUser.username());
    }

    private long count(List<CourseResult> list, CourseStatus status) {
        return list.stream().filter(r -> r.getStatus() == status).count();
    }

    private String percent(long part, long total) {
        return total == 0 ? "0.0" : String.valueOf(Math.round(part * PERCENT * ROUND / total) / ROUND);
    }

    private String yesNo(boolean value) {
        return value ? "Ha" : "Yo'q";
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private String typeLabel(GroupType type) {
        return type == GroupType.VOCATIONAL ? "Kasb kurslari" : "OTM tayyorlov kurslari";
    }
}
