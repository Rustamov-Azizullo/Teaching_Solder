package uz.askar.education.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record SecurityProperties(Cors cors, Security security, boolean seedDemoData) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Security(int maxFailedLogins, long lockMinutes) {
    }
}
