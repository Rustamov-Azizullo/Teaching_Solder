package uz.askar.education.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import uz.askar.education.locations.Location;
import uz.askar.education.locations.LocationService;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryDistrictRepository;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.security.Role;
import uz.askar.education.users.AppUser;
import uz.askar.education.users.AppUserRepository;

/**
 * Demo ma'lumot o'chirilganda ({@code app.seed-demo-data=false}) bo'sh bazaga tizimga kirish uchun kerakli
 * to'rt rolni yaratadi: megasuperadmin, superadmin, admin (okrug) va user (qism). Qolgan hudud va foydalanuvchilar
 * «Hududlar» sahifasidan qo'shiladi.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseUsersSeeder implements CommandLineRunner {

    private static final int GENERATED_PASSWORD_LENGTH = 16;
    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final java.security.SecureRandom random = new java.security.SecureRandom();

    private final SecurityProperties properties;
    private final AppUserRepository users;
    private final MilitaryDistrictRepository militaryDistricts;
    private final MilitaryUnitRepository militaryUnits;
    private final LocationService locationService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transaction;

    @Override
    public void run(String... args) {
        if (properties.seedDemoData() || users.count() > 0) {
            return;
        }
        transaction.executeWithoutResult(status -> seed());
    }

    private void seed() {
        MilitaryDistrict district = new MilitaryDistrict();
        district.setCode("MO-1");
        district.setName("1-harbiy okrug");
        district = militaryDistricts.save(district);

        MilitaryUnit unit = new MilitaryUnit();
        unit.setMilitaryDistrict(district);
        unit.setCode("Q-101");
        unit.setName("101-harbiy qism");
        unit = militaryUnits.save(unit);

        Location districtLocation = locationService.ensureForDistrict(district);
        Location unitLocation = locationService.ensureForUnit(unit);

        user("megasuperadmin", "Mega SuperAdmin", Role.MEGA_SUPER_ADMIN, null);
        user("superadmin", "SuperAdmin", Role.SUPER_ADMIN, null);
        user("admin", "Admin", Role.ADMIN, districtLocation);
        user("user", "User", Role.USER, unitLocation);
    }

    /** Sozlamada parol berilmasa, har bir hisobga tasodifiy parol yaratiladi va bir marta logga yoziladi. */
    private void user(String username, String fullName, Role role, Location location) {
        String configured = properties.initialPassword();
        String password = configured == null || configured.isBlank() ? generatePassword() : configured;
        if (configured == null || configured.isBlank()) {
            log.warn("Boshlang'ich hisob yaratildi: login={} parol={}", username, password);
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setLocation(location);
        users.save(user);
    }

    private String generatePassword() {
        StringBuilder password = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_ALPHABET.charAt(random.nextInt(PASSWORD_ALPHABET.length())));
        }
        return password.toString();
    }
}
