package uz.askar.education.reports;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.export.ExportFormat;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Hisobotlar", description = "XLSX / PDF eksport")
public class ReportController {

    private final ReportService service;

    @GetMapping("/{type}")
    public ResponseEntity<byte[]> report(
            @PathVariable ReportType type,
            @RequestParam(defaultValue = "XLSX") ExportFormat format,
            @RequestParam(required = false) Integer year) {
        var file = service.generate(type, year != null ? year : LocalDate.now().getYear(), format);
        return ResponseEntity.ok()
                .contentType(file.format().mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }
}
