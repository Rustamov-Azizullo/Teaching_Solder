package uz.askar.education.security;

/**
 * Ruxsatlar katalogi. Nomlar {@link Access} konstantalari va frontenddagi capability kalitlari bilan bir xil
 * (camelCase -&gt; UPPER_SNAKE_CASE). Qaysi rol qaysi ruxsatga ega ekanligi bazada ({@code role_permissions},
 * {@code user_permissions}) saqlanadi va SuperAdmin tomonidan ish vaqtida o'zgartiriladi.
 */
public enum Permission {
    ADMIN("Foydalanuvchilarni boshqarish"),
    SYSTEM_CONFIG("Tizim sozlamalari, sikllar, audit va integratsiya jurnallari"),
    DICTIONARY_WRITE("Ma'lumotnomalarni tahrirlash"),
    UNIT_DIRECTIONS("Qism kasb yo'nalishlarini belgilash"),
    SOLDIER_READ("Askarlarni ko'rish"),
    SOLDIER_WRITE("Askarlarni kiritish va tahrirlash"),
    QUESTIONNAIRE_READ("Anketalarni ko'rish"),
    QUESTIONNAIRE_WRITE("Anketalarni to'ldirish"),
    ATTACHMENT_WRITE("Fayllarni yuklash"),
    ASSIGNMENT_READ("Biriktirishlarni ko'rish"),
    ASSIGNMENT_PROPOSE("Biriktirish taklif qilish"),
    ASSIGNMENT_DECIDE("Biriktirish bo'yicha qaror qabul qilish"),
    DEADLINE_MANAGE("Muddatlarni boshqarish"),
    RESULT_READ("Kurs natijalarini ko'rish"),
    RESULT_WRITE("Kurs natijalarini kiritish"),
    ADMISSION_READ("OTMga qabulni ko'rish"),
    ADMISSION_WRITE("OTMga qabulni kiritish"),
    EMPLOYMENT("Bandlik ro'yxatlari"),
    TRANSFER("Askarni boshqa qismga o'tkazish"),
    GROUP_READ("Guruhlar va jadvalni ko'rish"),
    GROUP_WRITE("Guruhlarni boshqarish"),
    GROUP_LEADER_ASSIGN("Guruh kattasini tayinlash va kurs yakunini tasdiqlash"),
    SCHEDULE_WRITE("Dars jadvalini tuzish"),
    SCHEDULE_TIME_OVERRIDE("Standart dars vaqtini o'zgartirish"),
    REPORTS("Hisobotlarni eksport qilish"),
    DASHBOARD_VOCATIONAL("Kasb kurslari dashboardi"),
    DASHBOARD_OTM("OTM tayyorlov dashboardi"),
    DASHBOARD_SURVEYS("So'rovnomalar dashboardi");

    private final String label;

    Permission(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
