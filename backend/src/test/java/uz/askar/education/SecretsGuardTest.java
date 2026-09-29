package uz.askar.education;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import uz.askar.education.config.JwtProperties;
import uz.askar.education.config.SecretsGuard;
import uz.askar.education.config.StorageProperties;

class SecretsGuardTest {

    private static final String DEFAULT_JWT = "change-me-change-me-change-me-change-me-0123";
    private static final String STRONG_SECRET = "a-long-random-secret-value-that-is-not-a-default-1234567890";

    @Test
    void productionRefusesToStartWithDefaultSecrets() {
        MockEnvironment production = new MockEnvironment().withProperty("spring.datasource.password", "12345");
        production.setActiveProfiles("prod");
        SecretsGuard guard = new SecretsGuard(new JwtProperties(DEFAULT_JWT, 480),
                new StorageProperties("./data", "change-me-storage-secret"), production);

        assertThrows(IllegalStateException.class, guard::verify);
    }

    @Test
    void developmentOnlyWarnsAboutDefaultSecrets() {
        SecretsGuard guard = new SecretsGuard(new JwtProperties(DEFAULT_JWT, 480),
                new StorageProperties("./data", "change-me-storage-secret"), new MockEnvironment());

        assertDoesNotThrow(guard::verify);
    }

    @Test
    void productionStartsWithStrongSecrets() {
        MockEnvironment production = new MockEnvironment().withProperty("spring.datasource.password", STRONG_SECRET);
        production.setActiveProfiles("prod");
        SecretsGuard guard = new SecretsGuard(new JwtProperties(STRONG_SECRET, 480),
                new StorageProperties("./data", STRONG_SECRET + "-storage"), production);

        assertDoesNotThrow(guard::verify);
    }
}
