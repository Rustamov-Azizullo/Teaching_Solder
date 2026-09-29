package uz.askar.education.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.askar.education.common.TooManyRequestsException;
import uz.askar.education.config.SecurityProperties;

/** Bitta IP dan kirishga urinishlar sonini cheklaydi (hisobni bloklash orqali DoS va parol tanlashni qiyinlashtiradi). */
@Component
public class LoginRateLimiter {

    private static final long CLEANUP_INTERVAL_MS = 600_000;

    private final int maxAttempts;
    private final Duration window;
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public LoginRateLimiter(SecurityProperties properties) {
        this.maxAttempts = properties.login().maxAttemptsPerIp();
        this.window = Duration.ofMinutes(properties.login().windowMinutes());
    }

    /** Urinishni qayd etadi; limit oshgan bo'lsa, {@link TooManyRequestsException} tashlaydi. */
    public void register(String clientAddress) {
        Instant now = Instant.now();
        Deque<Instant> recent = attempts.computeIfAbsent(clientAddress, key -> new ArrayDeque<>());
        synchronized (recent) {
            drop(recent, now);
            if (recent.size() >= maxAttempts) {
                throw new TooManyRequestsException("Juda ko'p urinish. Birozdan keyin qayta urinib ko'ring");
            }
            recent.addLast(now);
        }
    }

    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    void evictExpired() {
        Instant now = Instant.now();
        attempts.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                drop(entry.getValue(), now);
                return entry.getValue().isEmpty();
            }
        });
    }

    private void drop(Deque<Instant> recent, Instant now) {
        while (!recent.isEmpty() && recent.peekFirst().isBefore(now.minus(window))) {
            recent.removeFirst();
        }
    }
}
