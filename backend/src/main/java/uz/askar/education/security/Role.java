package uz.askar.education.security;

/** TT 5-bo'lim: tizim rollari va ularning vakolat darajasi. */
public enum Role {
    SYSTEM_ADMIN("Tizim administratori", ScopeLevel.REPUBLIC),
    HKTB("MV HKTB xodimi", ScopeLevel.REPUBLIC),
    JTB("MV JTB xodimi", ScopeLevel.REPUBLIC),
    TMIBB("TMIBB / MBMM xodimi", ScopeLevel.REPUBLIC),
    DISTRICT_OFFICER("Harbiy okrug mas'uli", ScopeLevel.DISTRICT),
    UNIT_COMMANDER("Harbiy qism qo'mondoni", ScopeLevel.UNIT),
    UNIT_OPERATOR("Qism mas'ul xodimi (operator)", ScopeLevel.UNIT),
    COMBAT_TRAINING_DEPT("Qism jangovar tayyorgarlik bo'limi", ScopeLevel.UNIT),
    EDUCATION_DEPT("Qism tarbiyaviy ishlar bo'limi", ScopeLevel.UNIT),
    GROUP_LEADER("Guruh kattasi", ScopeLevel.UNIT),
    PSYCHOLOGIST("Harbiy psixolog (sotsiolog)", ScopeLevel.UNIT);

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
}
