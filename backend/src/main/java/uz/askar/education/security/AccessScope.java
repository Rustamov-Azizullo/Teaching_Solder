package uz.askar.education.security;

import uz.askar.education.common.ForbiddenException;

/** Foydalanuvchining vakolat doirasi: butun vazirlik, okrug yoki bitta qism. */
public record AccessScope(ScopeLevel level, Long districtId, Long unitId) {

    /**
     * Okrug/qism darajasidagi, lekin hududga biriktirilmagan foydalanuvchi uchun filtr qiymati: hech bir yozuvga
     * mos kelmaydi. {@code null} qaytarish "cheklovsiz" degani bo'lardi — bu xavfsiz emas.
     */
    private static final Long MATCHES_NOTHING = -1L;

    /** Repository so'rovlari uchun: okrug bo'yicha majburiy filtr (yoki {@code null} — cheklovsiz). */
    public Long districtFilter() {
        if (level != ScopeLevel.DISTRICT) {
            return null;
        }
        return districtId != null ? districtId : MATCHES_NOTHING;
    }

    /** Repository so'rovlari uchun: qism bo'yicha majburiy filtr (yoki {@code null} — cheklovsiz). */
    public Long unitFilter() {
        if (level != ScopeLevel.UNIT) {
            return null;
        }
        return unitId != null ? unitId : MATCHES_NOTHING;
    }

    public boolean covers(Long targetDistrictId, Long targetUnitId) {
        return switch (level) {
            case REPUBLIC -> true;
            case DISTRICT -> districtId != null && districtId.equals(targetDistrictId);
            case UNIT -> unitId != null && unitId.equals(targetUnitId);
        };
    }

    public void require(Long targetDistrictId, Long targetUnitId) {
        if (!covers(targetDistrictId, targetUnitId)) {
            throw new ForbiddenException("Ma'lumot vakolat doirasidan tashqarida");
        }
    }
}
