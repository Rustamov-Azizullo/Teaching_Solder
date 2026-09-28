package uz.askar.education.reports;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.export.ExportFormat;
import uz.askar.education.groups.GroupType;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Hisobotlar", description = "XLSX / PDF eksport")
public class ReportController {

    private static final int DEFAULT_PERIOD_DAYS = 7;

    private final ReportService service;

    @GetMapping("/{type}")
    public ResponseEntity<byte[]> report(
            @PathVariable ReportType type,
            @RequestParam(defaultValue = "XLSX") ExportFormat format,
            @RequestParam(defaultValue = "VOCATIONAL") GroupType groupType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_PERIOD_DAYS);
        var file = service.generate(type, groupType, start, end, format);
        return ResponseEntity.ok()
                .contentType(file.format().mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }
}
