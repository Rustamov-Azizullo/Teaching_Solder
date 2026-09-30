package uz.askar.education.reports;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.admissions.AdmissionService;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
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
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.soldiers.SoldierRepository;
import uz.askar.education.surveys.QuestionnaireRepository;

/**
 * Hisobotlar (TT M12): yagona uslubda — sarlavha, davr, vakolat doirasi, tuzilgan sana va tuzuvchi bilan.
 * Barcha hisobotlar vakolat doirasi bilan cheklanadi va eksport audit jurnaliga yoziladi.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    public record ReportFile(String fileName, byte[] content, ExportFormat format) {
    }

    private final WeeklySummaryBuilder weekly;
    private final ReportMeta meta;
    private final CourseResultRepository results;
    private final AdmissionService admissions;
    private final StudyGroupRepository groups;
    private final SoldierRepository soldiers;
    private final QuestionnaireRepository questionnaires;
    private final TableExporter exporter;
    private final CurrentUser currentUser;
    private final PermissionEvaluatorService permissions;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public ReportFile generate(ReportType type, int year, ExportFormat format, ReportTarget target) {
        requireAllowed(type);
        requireTargetOnlyForWeekly(type, target);
        requireCurrentYearWhereNeeded(type, year);
        TableData data = switch (type) {
            case WEEKLY_UNIT_SUMMARY -> weekly.build(year, target);
            case COURSE_COMPLETION -> courseCompletion(year);
            case OTM_ADMISSIONS -> otmAdmissions(year);
            case YEARLY_SUMMARY -> yearlySummary(year);
        };
        audit.record("EXPORT", "Report", type.name(), format + ", qatorlar: " + data.rows().size());
        return new ReportFile(type.name().toLowerCase() + "-" + LocalDate.now() + "." + format.extension(),
                exporter.export(data, format), format);
    }

    /** Qabul va yillik umumlashma joriy holatdan hisoblanadi, shuning uchun o'tgan yil so'ralsa, noto'g'ri sarlavhali hisobot chiqmasin. */
    private void requireCurrentYearWhereNeeded(ReportType type, int year) {
        boolean liveDataOnly = type == ReportType.OTM_ADMISSIONS || type == ReportType.YEARLY_SUMMARY;
        if (liveDataOnly && year != LocalDate.now().getYear()) {
            throw new BusinessRuleException("Bu hisobot faqat joriy yil uchun tuziladi");
        }
    }

    private void requireTargetOnlyForWeekly(ReportType type, ReportTarget target) {
        if (target.isSelection() && type != ReportType.WEEKLY_UNIT_SUMMARY) {
            throw new BusinessRuleException("Okrug, qism yoki bo'linma bo'yicha faqat haftalik hisobot tuziladi");
        }
    }

    private TableData courseCompletion(int year) {
        var scope = currentUser.scope();
        Map<Long, List<CourseResult>> byGroup = results.findInScope(GroupType.VOCATIONAL, scope.districtFilter(),
                scope.unitFilter()).stream().filter(r -> r.getGroup().getCycleYear() == year)
                .collect(Collectors.groupingBy(r -> r.getGroup().getId()));
        List<List<String>> table = byGroup.values().stream().map(list -> {
            var group = list.get(0).getGroup();
            return List.of(group.getMilitaryUnit().getName(), group.getName(),
                    group.getProfession() == null ? "" : group.getProfession().getName(),
                    String.valueOf(count(list, CourseStatus.STUDIED)), String.valueOf(count(list, CourseStatus.EXAM_PASSED)),
                    String.valueOf(count(list, CourseStatus.CERTIFIED)), String.valueOf(count(list, CourseStatus.DROPPED)),
                    group.getCourseApprovedAt() == null ? "Yo'q" : "Ha");
        }).sorted(Comparator.comparing((List<String> r) -> r.get(0)).thenComparing(r -> r.get(1))).toList();
        return new TableData("Kurs yakuni hisoboti (HKTB uchun)", meta.lines("Yillik sikl: " + year, null),
                List.of("Qism", "Guruh", "Kasb", "O'qidi", "Imtihondan o'tdi", "Sertifikat oldi", "Tugatmadi",
                        "Qo'mondon tasdiqladi"), table);
    }

    private TableData otmAdmissions(int year) {
        List<List<String>> table = admissions.list().stream().map(r -> List.of(r.fullName(), r.pinfl(), r.unitName(),
                yesNo(r.bmbaRegistered()), yesNo(r.testParticipated()), r.testScore() == null ? "" : r.testScore().toString(),
                yesNo(r.admitted()), nz(r.university()), nz(r.studyDirection()))).toList();
        return new TableData("OTMga qabul natijalari (HKTB uchun)", meta.lines("Yillik sikl: " + year, null),
                List.of("F.I.Sh.", "JShShIR", "Qism", "BMBA", "Test", "Ball", "Qabul", "OTM", "Yo'nalish"), table);
    }

    private TableData yearlySummary(int year) {
        var scope = currentUser.scope();
        Long d = scope.districtFilter();
        Long u = scope.unitFilter();
        var certified = results.findCertifiedInScope(d, u).size();
        List<List<String>> table = List.of(
                List.of("Jami askarlar", String.valueOf(soldiers.countInScope(d, u))),
                List.of("Kasb kurslari guruhlari", String.valueOf(groups.search(GroupType.VOCATIONAL, d, u).size())),
                List.of("OTM tayyorlov guruhlari", String.valueOf(groups.search(GroupType.OTM_PREP, d, u).size())),
                List.of("Yakunlangan anketalar", String.valueOf(questionnaires.findFinalizedInScope(year, d, u).size())),
                List.of("Sertifikat olganlar", String.valueOf(certified)),
                List.of("OTM nomzodlari", String.valueOf(admissions.list().size())),
                List.of("OTMga qabul qilinganlar", String.valueOf(admissions.list().stream().filter(r -> r.admitted()).count())));
        return new TableData("Vazirlik miqyosidagi yillik umumlashma", meta.lines("Yil: " + year, null),
                List.of("Ko'rsatkich", "Qiymat"), table);
    }

    /**
     * Hisobotni eksport qilish uchun {@code REPORTS} ruxsati va hisobot tarkibidagi barcha ma'lumotlarni
     * o'qish ruxsati talab qilinadi (masalan, yillik umumlashma askarlar, anketalar, natijalar va qabulni jamlaydi).
     */
    private void requireAllowed(ReportType type) {
        List<Permission> required = switch (type) {
            case WEEKLY_UNIT_SUMMARY -> List.of(Permission.REPORTS, Permission.SOLDIER_READ, Permission.GROUP_READ);
            case COURSE_COMPLETION -> List.of(Permission.REPORTS, Permission.RESULT_READ);
            case OTM_ADMISSIONS -> List.of(Permission.REPORTS, Permission.ADMISSION_READ);
            case YEARLY_SUMMARY -> List.of(Permission.REPORTS, Permission.SOLDIER_READ, Permission.QUESTIONNAIRE_READ,
                    Permission.RESULT_READ, Permission.ADMISSION_READ);
        };
        if (!required.stream().allMatch(permissions::currentUserHas)) {
            throw new ForbiddenException("Bu hisobot uchun vakolat yo'q");
        }
    }

    private long count(List<CourseResult> list, CourseStatus status) {
        return list.stream().filter(r -> r.getStatus() == status).count();
    }

    private String yesNo(boolean value) {
        return value ? "Ha" : "Yo'q";
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

}
