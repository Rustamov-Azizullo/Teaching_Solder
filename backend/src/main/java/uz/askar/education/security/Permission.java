package uz.askar.education.security;

/**
 * Ruxsatlar katalogi. Nomlar {@link Access} konstantalari va frontenddagi capability kalitlari bilan bir xil
 * (camelCase -&gt; UPPER_SNAKE_CASE). Qaysi rol qaysi ruxsatga ega ekanligi bazada ({@code role_permissions},
 * {@code user_permissions}) saqlanadi va SuperAdmin tomonidan ish vaqtida o'zgartiriladi.
 */
public enum Permission {
    ADMIN("Foydalanuvchilarni boshqarish (qo'shish, tahrirlash, o'chirish)"),
    SYSTEM_CONFIG("Tizim sozlamalari, sikllar, saqlash muddati, audit va integratsiya jurnallari"),
    DICTIONARY_WRITE("Ma'lumotnomalarni tahrirlash va o'chirish"),
    UNIT_DIRECTIONS("Qism uchun faol kasb yo'nalishlarini belgilash"),
    SOLDIER_READ("Askarlar ro'yxati va kartasini ko'rish"),
    SOLDIER_WRITE("Askarlarni kiritish, tahrirlash, Excel import va manba tizim bilan sinxronlash"),
    QUESTIONNAIRE_READ("Anketalarni ko'rish"),
    QUESTIONNAIRE_WRITE("Anketalarni to'ldirish"),
    ATTACHMENT_WRITE("Fayllarni yuklash va o'chirish"),
    DEADLINE_MANAGE("Muddatlarni yaratish, tahrirlash va bajarilgan deb belgilash"),
    RESULT_READ("Kurs natijalarini ko'rish"),
    RESULT_WRITE("Kurs natijalarini kiritish"),
    ADMISSION_READ("OTMga qabulni ko'rish"),
    ADMISSION_WRITE("OTMga qabulni kiritish va BMBA bilan sinxronlash"),
    EMPLOYMENT("Bandlik ro'yxatlarini ko'rish va eksport qilish"),
    TRANSFER("Askarni boshqa qismga o'tkazish"),
    GROUP_READ("Guruhlar, o'qituvchilar va dars jadvalini ko'rish"),
    GROUP_WRITE("Guruhlar, o'qituvchilar, muassasalar va bo'linmalarni boshqarish"),
    GROUP_LEADER_ASSIGN("Guruh kattasini kiritish/olib tashlash va kurs yakunini tasdiqlash"),
    REPORTS("Hisobotlarni ko'rish va eksport qilish"),
    DASHBOARD_VOCATIONAL("Kasb kurslari natijalari dashboardi"),
    DASHBOARD_OTM("OTM tayyorlov dashboardi"),
    DASHBOARD_SURVEYS("So'rovnoma va hududiy taqsimot dashboardi");

    private final String label;

    Permission(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
