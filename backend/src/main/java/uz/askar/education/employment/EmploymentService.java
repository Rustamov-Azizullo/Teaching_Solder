package uz.askar.education.employment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.export.ExportFormat;
import uz.askar.education.export.TableData;
import uz.askar.education.export.TableExporter;
import uz.askar.education.groups.GroupType;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.results.CourseResult;
import uz.askar.education.results.CourseResultRepository;
import uz.askar.education.results.CourseStatus;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.settings.SettingsService;

/**
 * Bandlik ro'yxatlari: xizmat tugashiga bir oy qolgan, kasbga o'qitilgan askarlar yashash hududi bo'yicha guruhlanadi
 * va tashqi idoralarga yuborish uchun XLSX/PDF eksport qilinadi. Eksportda faqat minimal maydonlar (qism ma'lumotisiz).
 */
@Service
@RequiredArgsConstructor
public class EmploymentService {

    private static final int DEFAULT_MONTHS = 1;
    private static final List<String> HEADERS = List.of("№", "F.I.Sh.", "Tug'ilgan sana", "JShShIR", "Tuman/shahar",
            "MFY", "Telefon", "Kasb", "Holat", "Sertifikat");

    public record Row(Long soldierId, String fullName, String birthDate, String pinfl, String district, String mahalla,
                      String phone, String profession, CourseStatus status, String certificateNo) {
    }

    public record RegionGroup(Long regionId, String regionName, List<Row> rows) {
    }

    public record ExportRequest(@NotBlank @Size(max = 200) String agency, Long regionId, @NotNull ExportFormat format) {
    }

    public record HistoryRow(Long id, String regionName, String agency, int soldierCount, String format,
                             LocalDateTime generatedAt, String generatedBy) {
    }

    public record ExportFile(String fileName, byte[] content, ExportFormat format) {
    }

    private final CourseResultRepository results;
    private final EmploymentListRepository lists;
    private final RegionRepository regions;
    private final SettingsService settings;
    private final TableExporter exporter;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<RegionGroup> preview(LocalDate today) {
        int months = months();
        LocalDate limit = today.plusMonths(months);
        var scope = currentUser.scope();
        Map<Long, List<Row>> byRegion = new LinkedHashMap<>();
        Map<Long, String> regionNames = new LinkedHashMap<>();
        results.findInScope(GroupType.VOCATIONAL, scope.districtFilter(), scope.unitFilter()).stream()
                .filter(r -> r.getStatus() != CourseStatus.DROPPED)
                .filter(r -> r.getSoldier().getServiceEndDate() != null
                        && !r.getSoldier().getServiceEndDate().isAfter(limit)
                        && !r.getSoldier().getServiceEndDate().isBefore(today))
                .sorted(Comparator.comparing(r -> r.getSoldier().getFullName()))
                .forEach(r -> {
                    var region = r.getSoldier().getRegion();
                    regionNames.put(region.getId(), region.getName());
                    byRegion.computeIfAbsent(region.getId(), k -> new java.util.ArrayList<>()).add(toRow(r));
                });
        return byRegion.entrySet().stream()
                .map(e -> new RegionGroup(e.getKey(), regionNames.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(RegionGroup::regionName)).toList();
    }

    @Transactional
    public ExportFile export(ExportRequest request) {
        List<RegionGroup> groups = preview(LocalDate.now()).stream()
                .filter(g -> request.regionId() == null || g.regionId().equals(request.regionId())).toList();
        List<Row> rows = groups.stream().flatMap(g -> g.rows().stream()).toList();
        if (rows.isEmpty()) {
            throw new BusinessRuleException("Eksport qilish uchun ro'yxatda askarlar yo'q");
        }
        List<List<String>> table = new java.util.ArrayList<>();
        int index = 1;
        for (Row r : rows) {
            table.add(List.of(String.valueOf(index++), r.fullName(), r.birthDate(), r.pinfl(), nz(r.district()),
                    nz(r.mahalla()), nz(r.phone()), nz(r.profession()), statusLabel(r.status()), nz(r.certificateNo())));
        }
        String regionName = request.regionId() == null ? "Barcha hududlar" : groups.get(0).regionName();
        TableData data = new TableData("Bandlik uchun ro'yxat", List.of("Qabul qiluvchi idora: " + request.agency(),
                "Hudud: " + regionName, "Tuzilgan sana: " + LocalDate.now(), "Tuzuvchi: " + currentUser.username()),
                HEADERS, table);
        byte[] content = exporter.export(data, request.format());

        EmploymentList record = new EmploymentList();
        record.setRegion(request.regionId() == null ? null : regions.findById(request.regionId()).orElse(null));
        record.setAgency(request.agency());
        record.setSoldierCount(rows.size());
        record.setFormat(request.format().name());
        record.setGeneratedAt(LocalDateTime.now());
        record.setGeneratedBy(currentUser.username());
        record.setCycleYear(LocalDate.now().getYear());
        lists.save(record);
        audit.record("EXPORT", "EmploymentList", record.getId(), request.agency() + ", " + rows.size() + " ta, " + regionName);
        return new ExportFile("bandlik-royxati." + request.format().extension(), content, request.format());
    }

    @Transactional(readOnly = true)
    public List<HistoryRow> history() {
        return lists.findTop100ByOrderByGeneratedAtDesc().stream()
                .map(l -> new HistoryRow(l.getId(), l.getRegion() == null ? "Barcha hududlar" : l.getRegion().getName(),
                        l.getAgency(), l.getSoldierCount(), l.getFormat(), l.getGeneratedAt(), l.getGeneratedBy()))
                .collect(Collectors.toList());
    }

    /** Foydalanuvchi kontekstisiz (rejalashtirilgan ish uchun): butun vazirlik bo'yicha soni. */
    @Transactional(readOnly = true)
    public int previewSystem(LocalDate today) {
        LocalDate limit = today.plusMonths(months());
        return (int) results.findInScope(GroupType.VOCATIONAL, null, null).stream()
                .filter(r -> r.getStatus() != CourseStatus.DROPPED)
                .filter(r -> r.getSoldier().getServiceEndDate() != null
                        && !r.getSoldier().getServiceEndDate().isAfter(limit)
                        && !r.getSoldier().getServiceEndDate().isBefore(today)).count();
    }

    private int months() {
        try {
            return Integer.parseInt(settings.get("employment.monthsBeforeEnd", String.valueOf(DEFAULT_MONTHS)));
        } catch (NumberFormatException ex) {
            return DEFAULT_MONTHS;
        }
    }

    private Row toRow(CourseResult r) {
        var s = r.getSoldier();
        return new Row(s.getId(), s.getFullName(), s.getBirthDate().toString(), s.getPinfl(), s.getDistrict().getName(),
                s.getMahalla(), s.getPhone(), r.getGroup().getProfession() == null ? null : r.getGroup().getProfession().getName(),
                r.getStatus(), r.getCertificateNo());
    }

    private String nz(String value) {
        return value == null ? "" : value;
    }

    private String statusLabel(CourseStatus status) {
        return switch (status) {
            case STUDIED -> "O'qidi";
            case EXAM_PASSED -> "Imtihondan o'tdi";
            case CERTIFIED -> "Sertifikat oldi";
            case DROPPED -> "O'qishni tugatmadi";
        };
    }
}
