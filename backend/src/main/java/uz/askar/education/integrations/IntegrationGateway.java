package uz.askar.education.integrations;

import java.time.LocalDateTime;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Tashqi tizimga har bir murojaat vaqti, holati va xatosi bilan jurnallanadi (TT M14, 7.3). */
@Service
@RequiredArgsConstructor
public class IntegrationGateway {

    private static final int MAX_MESSAGE = 500;

    private final IntegrationLogRepository logs;
    private final PlatformTransactionManager transactionManager;

    public <T> T call(String system, String operation, String reference, Supplier<T> action) {
        long started = System.currentTimeMillis();
        try {
            T result = action.get();
            record(system, operation, reference, true, "OK", System.currentTimeMillis() - started);
            return result;
        } catch (RuntimeException ex) {
            String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            record(system, operation, reference, false, message, System.currentTimeMillis() - started);
            throw ex;
        }
    }

    /** Har doim alohida tranzaksiyada yoziladi (chaqiruvchi tranzaksiya faqat o'qish bo'lsa ham, xato bilan tugasa ham). */
    public void record(String system, String operation, String reference, boolean success, String message,
                       long durationMs) {
        IntegrationLog entry = new IntegrationLog();
        entry.setAt(LocalDateTime.now());
        entry.setSystem(system);
        entry.setOperation(operation);
        entry.setReference(reference);
        entry.setSuccess(success);
        entry.setMessage(message.length() > MAX_MESSAGE ? message.substring(0, MAX_MESSAGE) : message);
        entry.setDurationMs(durationMs);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        entry.setActor(authentication == null ? "system" : authentication.getName());
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status -> logs.save(entry));
    }
}
