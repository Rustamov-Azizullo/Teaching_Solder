package uz.askar.education.audit;

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
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit jurnali")
public class AuditController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository logs;

    @GetMapping
    @PreAuthorize(Access.SYSTEM_CONFIG)
    public PageResponse<AuditLogDto> search(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String entity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        var result = logs.search(username, entity, PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE)));
        return PageResponse.from(result, AuditLogDto::from);
    }

    public record AuditLogDto(Long id, LocalDateTime at, String username, String action, String entity,
                              String entityId, String details) {

        static AuditLogDto from(AuditLog log) {
            return new AuditLogDto(log.getId(), log.getAt(), log.getUsername(), log.getAction(), log.getEntity(),
                    log.getEntityId(), log.getDetails());
        }
    }
}
