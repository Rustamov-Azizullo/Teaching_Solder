package uz.askar.education.employment;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.employment.EmploymentService.ExportRequest;
import uz.askar.education.employment.EmploymentService.HistoryRow;
import uz.askar.education.employment.EmploymentService.RegionGroup;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/employment")
@RequiredArgsConstructor
@Tag(name = "Bandlik ro'yxatlari", description = "M11: hududlar bo'yicha guruhlangan, XLSX/PDF eksport")
public class EmploymentController {

    private final EmploymentService service;

    @GetMapping("/preview")
    @PreAuthorize(Access.EMPLOYMENT)
    public List<RegionGroup> preview() {
        return service.preview(LocalDate.now());
    }

    @PostMapping("/export")
    @PreAuthorize(Access.EMPLOYMENT)
    public ResponseEntity<byte[]> export(@Valid @RequestBody ExportRequest request) {
        var file = service.export(request);
        return ResponseEntity.ok()
                .contentType(file.format().mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.content());
    }

    @GetMapping("/history")
    @PreAuthorize(Access.EMPLOYMENT)
    public List<HistoryRow> history() {
        return service.history();
    }
}
