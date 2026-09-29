package uz.askar.education.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Standart (ochiq) maxfiy qiymatlar bilan ishga tushishni tekshiradi: {@code prod} profilida ishga tushirishni
 * to'xtatadi, boshqa profillarda ogohlantiradi. JWT kaliti ochiq bo'lsa, istalgan odam token yasay oladi.
 */
@Slf4j
@Component
public class SecretsGuard {

    static final String DEFAULT_JWT_SECRET = "change-me-change-me-change-me-change-me-0123";
    static final String DEFAULT_STORAGE_SECRET = "change-me-storage-secret";
    private static final List<String> DEFAULT_DB_PASSWORDS = List.of("12345", "postgres");
    private static final String PRODUCTION_PROFILE = "prod";

    private final JwtProperties jwt;
    private final StorageProperties storage;
    private final Environment environment;

    public SecretsGuard(JwtProperties jwt, StorageProperties storage, Environment environment) {
        this.jwt = jwt;
        this.storage = storage;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verify() {
        List<String> problems = problems();
        if (problems.isEmpty()) {
            return;
        }
        String message = "Standart maxfiy qiymatlar ishlatilmoqda: " + String.join(", ", problems);
        if (environment.acceptsProfiles(Profiles.of(PRODUCTION_PROFILE))) {
            throw new IllegalStateException(message + ". Ularni muhit o'zgaruvchilari orqali almashtiring");
        }
        log.warn("{} — productionda ({} profili) ishga tushirish rad etiladi", message, PRODUCTION_PROFILE);
    }

    List<String> problems() {
        List<String> result = new java.util.ArrayList<>();
        if (DEFAULT_JWT_SECRET.equals(jwt.secret())) {
            result.add("JWT_SECRET");
        }
        if (DEFAULT_STORAGE_SECRET.equals(storage.secret())) {
            result.add("STORAGE_SECRET");
        }
        String databasePassword = environment.getProperty("spring.datasource.password");
        if (databasePassword != null && DEFAULT_DB_PASSWORDS.contains(databasePassword)) {
            result.add("DB_PASSWORD");
        }
        return result;
    }
}
