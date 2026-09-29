package uz.askar.education.audit;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Audit jurnali: kim, qachon, qaysi yozuvni o'zgartirdi (TT M14). Yozuvlar faqat qo'shiladi.
 * Faol tranzaksiya ichida chaqirilsa, yozuv tranzaksiya muvaffaqiyatli yakunlangandan keyin saqlanadi:
 * bekor qilingan yoki commit vaqtida xatoga uchragan amal jurnalda "bajarildi" bo'lib qolmaydi.
 */
@Slf4j
@Service
public class AuditService {

    private static final int MAX_DETAILS_LENGTH = 1000;
    private static final String SYSTEM_ACTOR = "system";

    private final AuditLogRepository logs;
    private final TransactionTemplate independentTransaction;

    public AuditService(AuditLogRepository logs, PlatformTransactionManager transactionManager) {
        this.logs = logs;
        this.independentTransaction = new TransactionTemplate(transactionManager);
        this.independentTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(String action, String entity, Object entityId, String details) {
        record(currentActor(), action, entity, entityId, details);
    }

    public void record(String actor, String action, String entity, Object entityId, String details) {
        AuditLog entry = new AuditLog();
        entry.setAt(LocalDateTime.now());
        entry.setUsername(actor);
        entry.setAction(action);
        entry.setEntity(entity);
        entry.setEntityId(entityId == null ? null : String.valueOf(entityId));
        entry.setDetails(truncate(details));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    save(entry);
                }
            });
        } else {
            save(entry);
        }
    }

    /** Jurnalga yozishdagi xato allaqachon bajarilgan asosiy amalni buzmasligi kerak, shuning uchun faqat loglanadi. */
    private void save(AuditLog entry) {
        try {
            independentTransaction.executeWithoutResult(status -> logs.save(entry));
        } catch (RuntimeException ex) {
            log.error("Audit yozuvini saqlab bo'lmadi: {} {}", entry.getAction(), entry.getEntity(), ex);
        }
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
