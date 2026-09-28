package uz.askar.education.security;

/**
 * Tizim rollari va ularning vakolat darajasi. Rol faqat hududiy doirani (respublika / okrug / qism) belgilaydi;
 * aniq amallar {@link Permission} orqali — rol-ruxsat matritsasi va foydalanuvchiga shaxsiy qo'shimcha ruxsatlar
 * bilan — boshqariladi (qarang: {@link PermissionEvaluatorService}).
 */
public enum Role {
    MEGA_SUPER_ADMIN("Mega SuperAdmin", ScopeLevel.REPUBLIC),
    SUPER_ADMIN("SuperAdmin", ScopeLevel.REPUBLIC),
    ADMIN("Admin", ScopeLevel.DISTRICT),
    USER("User", ScopeLevel.UNIT);

    private final String label;
    private final ScopeLevel scopeLevel;

    Role(String label, ScopeLevel scopeLevel) {
        this.label = label;
        this.scopeLevel = scopeLevel;
    }

    public String label() {
        return label;
    }

    public ScopeLevel scopeLevel() {
        return scopeLevel;
    }

    /** SuperAdmin va Mega SuperAdmin har qanday ruxsat tekshiruvidan har doim o'tadi (sozlanmaydi). */
    public boolean isAlwaysAllowed() {
        return this == MEGA_SUPER_ADMIN || this == SUPER_ADMIN;
    }

    /** Rol-ruxsat matritsasida saqlanadigan (tahrirlanadigan) rollar. */
    public boolean isConfigurable() {
        return !isAlwaysAllowed();
    }

    /** Ierarxiyada bu rol {@code other} dan yuqori yoki unga teng (MEGA_SUPER_ADMIN eng yuqori). */
    public boolean isAtLeast(Role other) {
        return ordinal() <= other.ordinal();
    }
}
