package uz.askar.education.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record SecurityProperties(Cors cors, Security security, boolean seedDemoData, String initialPassword,
                                 Login login) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Security(int maxFailedLogins, long lockMinutes) {
    }

    /** Bitta IP dan {@code windowMinutes} ichida qabul qilinadigan kirish urinishlari soni. */
    public record Login(int maxAttemptsPerIp, long windowMinutes) {
    }
}
