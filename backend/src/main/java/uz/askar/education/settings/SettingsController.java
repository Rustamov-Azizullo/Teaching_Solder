package uz.askar.education.settings;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Tizim sozlamalari")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public Map<String, String> all() {
        return settingsService.all();
    }

    @PutMapping
    @PreAuthorize(Access.ADMIN)
    public Map<String, String> update(@RequestBody Map<String, String> changes) {
        settingsService.update(changes);
        return settingsService.all();
    }
}
