package uz.askar.education.dictionaries;

/** Dinamik ma'lumotnoma turlari (TT M1). Yangi element qo'shish kodni o'zgartirmaydi. */
public enum DictionaryType {
    SUBJECT("Fanlar"),
    PROFESSION("Kasblar"),
    PROFESSION_DIRECTION("Kasb yo'nalishlari"),
    FUTURE_PLAN("Kelgusi reja variantlari"),
    LANGUAGE("Tillar"),
    KINSHIP("Qarindoshlik darajalari"),
    AWARD_KIND("Sovrindorlik turlari");

    private final String label;

    DictionaryType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
