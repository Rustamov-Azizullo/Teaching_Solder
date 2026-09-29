package uz.askar.education.integrations;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.common.PageResponse;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/integration-logs")
@RequiredArgsConstructor
@Tag(name = "Integratsiya jurnali")
public class IntegrationLogController {

    private final IntegrationLogRepository logs;

    public record IntegrationLogDto(Long id, LocalDateTime at, String system, String operation, String reference,
                                    boolean success, String message, long durationMs, String actor) {
    }

    @GetMapping
    @PreAuthorize(Access.SYSTEM_CONFIG)
    public PageResponse<IntegrationLogDto> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "30") int size) {
        return PageResponse.from(logs.findAllByOrderByAtDesc(PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)))),
                l -> new IntegrationLogDto(l.getId(), l.getAt(), l.getSystem(), l.getOperation(), l.getReference(),
                        l.isSuccess(), l.getMessage(), l.getDurationMs(), l.getActor()));
    }
}
