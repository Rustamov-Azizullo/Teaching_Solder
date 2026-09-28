package uz.askar.education.retention;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/retention")
@RequiredArgsConstructor
@Tag(name = "Saqlash muddati")
public class RetentionController {

    private final RetentionService service;

    @PostMapping("/run")
    @PreAuthorize(Access.ADMIN)
    public Map<String, Integer> run() {
        LocalDate today = LocalDate.now();
        return Map.of("warned", service.warn(today), "anonymized", service.anonymizeDue(today));
    }
}
