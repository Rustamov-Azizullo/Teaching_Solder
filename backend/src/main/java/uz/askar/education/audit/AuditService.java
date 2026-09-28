package uz.askar.education.audit;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Audit jurnali: kim, qachon, qaysi yozuvni o'zgartirdi (TT M14). Yozuvlar faqat qo'shiladi. */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_DETAILS_LENGTH = 1000;
    private static final String SYSTEM_ACTOR = "system";

    private final AuditLogRepository logs;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entity, Object entityId, String details) {
        record(currentActor(), action, entity, entityId, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String entity, Object entityId, String details) {
        AuditLog entry = new AuditLog();
        entry.setAt(LocalDateTime.now());
        entry.setUsername(actor);
        entry.setAction(action);
        entry.setEntity(entity);
        entry.setEntityId(entityId == null ? null : String.valueOf(entityId));
        entry.setDetails(truncate(details));
        logs.save(entry);
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : SYSTEM_ACTOR;
    }

    private String truncate(String details) {
        if (details == null || details.length() <= MAX_DETAILS_LENGTH) {
            return details;
        }
        return details.substring(0, MAX_DETAILS_LENGTH);
    }
}
