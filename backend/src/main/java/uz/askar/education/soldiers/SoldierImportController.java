package uz.askar.education.soldiers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/soldiers/import")
@RequiredArgsConstructor
@Tag(name = "Askarlarni ommaviy import", description = "XLSX shablon orqali")
public class SoldierImportController {

    private final SoldierImportService importService;

    @GetMapping("/template")
    @PreAuthorize(Access.SOLDIER_WRITE)
    public ResponseEntity<byte[]> template() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"askarlar-shablon.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(importService.template());
    }

    @PostMapping
    @PreAuthorize(Access.SOLDIER_WRITE)
    public SoldierImportService.ImportResult importFile(@RequestParam("file") MultipartFile file) {
        return importService.importFile(file);
    }
}
