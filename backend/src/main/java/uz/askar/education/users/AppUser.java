package uz.askar.education.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.security.Role;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "military_district_id")
    private MilitaryDistrict militaryDistrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "military_unit_id")
    private MilitaryUnit militaryUnit;

    private boolean active = true;
    private int failedAttempts;
    private LocalDateTime lockedUntil;

    @Column(name = "totp_secret")
    private String totpSecret;

    private boolean totpEnabled;

    /** Okrug darajasidagi foydalanuvchi uchun okrug, qism darajasidagi uchun qismning okrugi. */
    public Long effectiveDistrictId() {
        if (militaryDistrict != null) {
            return militaryDistrict.getId();
        }
        return militaryUnit != null ? militaryUnit.getMilitaryDistrict().getId() : null;
    }
}
