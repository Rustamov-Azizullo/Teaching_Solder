package uz.askar.education.reports;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.groups.StudyGroupRepository;
import uz.askar.education.results.CourseResult;
import uz.askar.education.results.CourseResultRepository;
import uz.askar.education.results.CourseStatus;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.Permission;
import uz.askar.education.security.PermissionEvaluatorService;
import uz.askar.education.soldiers.Soldier;
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
    public ReportFile generate(ReportType type, int year, ExportFormat format) {
        requireAllowed(type);
        requireCurrentYearWhereNeeded(type, year);
        TableData data = switch (type) {
            case WEEKLY_UNIT_SUMMARY -> weeklyUnitSummary(year);
            case COURSE_COMPLETION -> courseCompletion(year);
            case OTM_ADMISSIONS -> otmAdmissions(year);
            case YEARLY_SUMMARY -> yearlySummary(year);
        };
        audit.record("EXPORT", "Report", type.name(), format + ", qatorlar: " + data.rows().size());
        return new ReportFile(type.name().toLowerCase() + "-" + LocalDate.now() + "." + format.extension(),
                exporter.export(data, format), format);
    }

    /**
     * Haftalik hisobot okruglar uchun: har bir harbiy qism bo'yicha jami askarlar, kasb kursi va OTM tayyorlovdagilar,
     * kasb guruhlari (kasb va askarlar soni) hamda OTM yo'nalishlari (fanlar) bo'yicha askarlar soni.
     */
    private TableData weeklyUnitSummary(int year) {
        var scope = currentUser.scope();
        Map<String, List<Soldier>> soldiersByUnit = soldiers.findAllInScope(scope.districtFilter(), scope.unitFilter())
                .stream().collect(Collectors.groupingBy(WeeklyUnitRows::unitKey));
        List<StudyGroup> vocational = currentGroups(GroupType.VOCATIONAL, year);
        List<StudyGroup> otm = currentGroups(GroupType.OTM_PREP, year);
        Set<String> unitKeys = new LinkedHashSet<>(soldiersByUnit.keySet());
        unitKeys.addAll(vocational.stream().map(WeeklyUnitRows::unitKey).toList());
        unitKeys.addAll(otm.stream().map(WeeklyUnitRows::unitKey).toList());

        List<List<String>> table = new ArrayList<>();
        unitKeys.stream().sorted().forEach(key -> table.addAll(WeeklyUnitRows.forUnit(key,
                soldiersByUnit.getOrDefault(key, List.of()),
                vocational.stream().filter(g -> WeeklyUnitRows.unitKey(g).equals(key)).toList(),
                otm.stream().filter(g -> WeeklyUnitRows.unitKey(g).equals(key)).toList())));
        return new TableData("Haftalik hisobot: harbiy qismlar kesimida", meta("Yillik sikl: " + year),
                WeeklyUnitRows.HEADERS, table);
    }

    /** Qabul va yillik umumlashma joriy holatdan hisoblanadi, shuning uchun o'tgan yil so'ralsa, noto'g'ri sarlavhali hisobot chiqmasin. */
    private void requireCurrentYearWhereNeeded(ReportType type, int year) {
        boolean liveDataOnly = type == ReportType.OTM_ADMISSIONS || type == ReportType.YEARLY_SUMMARY;
        if (liveDataOnly && year != LocalDate.now().getYear()) {
            throw new BusinessRuleException("Bu hisobot faqat joriy yil uchun tuziladi");
        }
    }

    private List<StudyGroup> currentGroups(GroupType type, int year) {
        var scope = currentUser.scope();
        return groups.search(type, scope.districtFilter(), scope.unitFilter()).stream()
                .filter(group -> group.getCycleYear() == year).toList();
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
        return new TableData("Kurs yakuni hisoboti (HKTB uchun)", meta("Yillik sikl: " + year),
                List.of("Qism", "Guruh", "Kasb", "O'qidi", "Imtihondan o'tdi", "Sertifikat oldi", "Tugatmadi",
                        "Qo'mondon tasdiqladi"), table);
    }

    private TableData otmAdmissions(int year) {
        List<List<String>> table = admissions.list().stream().map(r -> List.of(r.fullName(), r.pinfl(), r.unitName(),
                yesNo(r.bmbaRegistered()), yesNo(r.testParticipated()), r.testScore() == null ? "" : r.testScore().toString(),
                yesNo(r.admitted()), nz(r.university()), nz(r.studyDirection()))).toList();
        return new TableData("OTMga qabul natijalari (HKTB uchun)", meta("Yillik sikl: " + year),
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
        return new TableData("Vazirlik miqyosidagi yillik umumlashma", meta("Yil: " + year),
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

    private List<String> meta(String period) {
        var scope = currentUser.scope();
        String area = switch (scope.level()) {
            case REPUBLIC -> "Vazirlik";
            case DISTRICT -> "Harbiy okrug #" + scope.districtId();
            case UNIT -> "Harbiy qism #" + scope.unitId();
        };
        return List.of(period, "Vakolat doirasi: " + area, "Tuzilgan sana: " + LocalDate.now(),
                "Tuzuvchi: " + currentUser.username());
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
