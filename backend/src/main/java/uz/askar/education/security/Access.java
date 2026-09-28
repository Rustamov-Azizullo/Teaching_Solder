package uz.askar.education.security;

/**
 * {@code @PreAuthorize} ifodalari. Har bir konstanta {@link Permission} katalogidagi bir xil nomli ruxsatni
 * {@link PermissionEvaluatorService} ({@code @perm}) orqali tekshiradi; qaysi rol/foydalanuvchi qaysi ruxsatga
 * ega ekanligi bazada saqlanadi va SuperAdmin tomonidan ish vaqtida o'zgartiriladi.
 */
public final class Access {

    private Access() {
    }

    /** Ruxsatlarning o'zini boshqarish — statik, dinamik tizimdan tashqarida (aylanma bog'liqlik bo'lmasligi uchun). */
    public static final String PERMISSION_MANAGE = "hasAnyRole('SUPER_ADMIN','MEGA_SUPER_ADMIN')";

    /** Hududlar daraxtini tahrirlash (qo'shish, o'zgartirish, o'chirish) — faqat SuperAdmin va undan yuqori. */
    public static final String LOCATION_MANAGE = "hasAnyRole('SUPER_ADMIN','MEGA_SUPER_ADMIN')";

    /** Hududlar ro'yxati (foydalanuvchini biriktirish uchun) — statik rol tekshiruvi. */
    public static final String USER_ADMIN_ROLES = "hasAnyRole('ADMIN','SUPER_ADMIN','MEGA_SUPER_ADMIN')";

    public static final String ADMIN = "@perm.has(authentication,'ADMIN')";

    public static final String SYSTEM_CONFIG = "@perm.has(authentication,'SYSTEM_CONFIG')";

    public static final String DICTIONARY_WRITE = "@perm.has(authentication,'DICTIONARY_WRITE')";

    public static final String UNIT_DIRECTIONS = "@perm.has(authentication,'UNIT_DIRECTIONS')";

    public static final String SOLDIER_WRITE = "@perm.has(authentication,'SOLDIER_WRITE')";

    public static final String SOLDIER_READ = "@perm.has(authentication,'SOLDIER_READ')";

    public static final String QUESTIONNAIRE_WRITE = "@perm.has(authentication,'QUESTIONNAIRE_WRITE')";

    public static final String QUESTIONNAIRE_READ = "@perm.has(authentication,'QUESTIONNAIRE_READ')";

    public static final String ATTACHMENT_WRITE = "@perm.has(authentication,'ATTACHMENT_WRITE')";

    public static final String ASSIGNMENT_PROPOSE = "@perm.has(authentication,'ASSIGNMENT_PROPOSE')";

    public static final String ASSIGNMENT_DECIDE = "@perm.has(authentication,'ASSIGNMENT_DECIDE')";

    public static final String ASSIGNMENT_READ = "@perm.has(authentication,'ASSIGNMENT_READ')";

    public static final String DEADLINE_MANAGE = "@perm.has(authentication,'DEADLINE_MANAGE')";

    public static final String RESULT_WRITE = "@perm.has(authentication,'RESULT_WRITE')";

    public static final String RESULT_READ = "@perm.has(authentication,'RESULT_READ')";

    public static final String ADMISSION_WRITE = "@perm.has(authentication,'ADMISSION_WRITE')";

    public static final String ADMISSION_READ = "@perm.has(authentication,'ADMISSION_READ')";

    public static final String EMPLOYMENT = "@perm.has(authentication,'EMPLOYMENT')";

    public static final String TRANSFER = "@perm.has(authentication,'TRANSFER')";

    public static final String GROUP_WRITE = "@perm.has(authentication,'GROUP_WRITE')";

    public static final String GROUP_LEADER_ASSIGN = "@perm.has(authentication,'GROUP_LEADER_ASSIGN')";

    public static final String GROUP_READ = "@perm.has(authentication,'GROUP_READ')";

    public static final String SCHEDULE_WRITE = "@perm.has(authentication,'SCHEDULE_WRITE')";

    public static final String REPORTS = "@perm.has(authentication,'REPORTS')";

    public static final String DASHBOARD_VOCATIONAL = "@perm.has(authentication,'DASHBOARD_VOCATIONAL')";

    public static final String DASHBOARD_OTM = "@perm.has(authentication,'DASHBOARD_OTM')";

    public static final String DASHBOARD_SURVEYS = "@perm.has(authentication,'DASHBOARD_SURVEYS')";
}
