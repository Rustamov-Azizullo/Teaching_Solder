package uz.askar.education.security;

import uz.askar.education.common.ForbiddenException;

/** Foydalanuvchining vakolat doirasi: butun respublika, okrug yoki bitta qism. */
public record AccessScope(ScopeLevel level, Long districtId, Long unitId) {

    /** Repository so'rovlari uchun: okrug bo'yicha majburiy filtr (yoki {@code null} — cheklovsiz). */
    public Long districtFilter() {
        return level == ScopeLevel.DISTRICT ? districtId : null;
    }

    /** Repository so'rovlari uchun: qism bo'yicha majburiy filtr (yoki {@code null} — cheklovsiz). */
    public Long unitFilter() {
        return level == ScopeLevel.UNIT ? unitId : null;
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
