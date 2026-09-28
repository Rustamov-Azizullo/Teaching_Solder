package uz.askar.education.results;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.askar.education.results.KtaComparisonService.Mismatch;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/results/kta-comparison")
@RequiredArgsConstructor
@Tag(name = "KTA bilan solishtirish", description = "API orqali yoki zaxira XLSX")
public class KtaController {

    private final KtaComparisonService service;

    @GetMapping
    @PreAuthorize(Access.KTA_COMPARE)
    public List<Mismatch> viaApi() {
        return service.compareViaApi();
    }

    @PostMapping
    @PreAuthorize(Access.KTA_COMPARE)
    public List<Mismatch> viaFile(@RequestParam("file") MultipartFile file) {
        return service.compareViaFile(file);
    }
}
