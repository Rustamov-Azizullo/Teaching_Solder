package uz.askar.education.users;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.askar.education.security.Role;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AppUser> findByRoleInAndActiveTrue(Collection<Role> roles);

    /** Qism hududiga biriktirilgan faol foydalanuvchilar. */
    List<AppUser> findByRoleAndLocationMilitaryUnitIdAndActiveTrue(Role role, Long unitId);

    List<AppUser> findByLocationMilitaryUnitIdAndActiveTrue(Long unitId);

    /**
     * Okrug doirasidagi foydalanuvchilar (okrugning o'ziga yoki uning qismlariga biriktirilganlar);
     * {@code districtId = null} bo'lsa — barchasi.
     */
    @Query("""
            select u from AppUser u
            left join u.location l
            left join l.parent p
            left join l.militaryDistrict ownDistrict
            left join p.militaryDistrict parentDistrict
            where :districtId is null
               or ownDistrict.id = :districtId
               or parentDistrict.id = :districtId
            order by u.fullName
            """)
    List<AppUser> findInDistrictScope(@Param("districtId") Long districtId);

    /** Token tekshiruvi uchun: foydalanuvchi va uning hududiy biriktirilishi bitta so'rovda. */
    @Query("""
            select u from AppUser u
            left join fetch u.location l
            left join fetch l.parent
            where u.id = :id
            """)
    Optional<AppUser> findWithLocationById(@Param("id") Long id);
}
