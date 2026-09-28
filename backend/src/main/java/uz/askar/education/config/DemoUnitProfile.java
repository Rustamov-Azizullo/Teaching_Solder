package uz.askar.education.config;

import java.util.EnumSet;
import java.util.Set;
import uz.askar.education.security.Permission;

/**
 * Qism darajasidagi demo foydalanuvchilarning vazifaga xos shaxsiy ruxsatlari. USER rolining umumiy ruxsatlari
 * ({@code GROUP_READ}, V11 migratsiyasi) ustiga qo'shiladi. V11 migratsiyasi mavjud bazadagi eski qism rollari
 * uchun aynan shu to'plamlarni {@code user_permissions} ga ko'chiradi.
 */
enum DemoUnitProfile {
    COMMANDER(EnumSet.of(
            Permission.SOLDIER_READ, Permission.SOLDIER_WRITE,
            Permission.QUESTIONNAIRE_READ, Permission.QUESTIONNAIRE_WRITE,
            Permission.ATTACHMENT_WRITE,
            Permission.ASSIGNMENT_READ, Permission.ASSIGNMENT_PROPOSE,
            Permission.RESULT_READ, Permission.RESULT_WRITE,
            Permission.ADMISSION_READ, Permission.ADMISSION_WRITE,
            Permission.EMPLOYMENT, Permission.TRANSFER,
            Permission.GROUP_WRITE, Permission.GROUP_LEADER_ASSIGN,
            Permission.SCHEDULE_WRITE, Permission.SCHEDULE_TIME_OVERRIDE,
            Permission.UNIT_DIRECTIONS, Permission.REPORTS,
            Permission.DASHBOARD_VOCATIONAL, Permission.DASHBOARD_OTM, Permission.DASHBOARD_SURVEYS)),
    OPERATOR(EnumSet.of(
            Permission.SOLDIER_READ, Permission.SOLDIER_WRITE,
            Permission.QUESTIONNAIRE_READ,
            Permission.ATTACHMENT_WRITE,
            Permission.ASSIGNMENT_READ, Permission.ASSIGNMENT_PROPOSE,
            Permission.RESULT_READ, Permission.RESULT_WRITE,
            Permission.ADMISSION_READ, Permission.ADMISSION_WRITE,
            Permission.EMPLOYMENT,
            Permission.GROUP_WRITE,
            Permission.SCHEDULE_WRITE,
            Permission.UNIT_DIRECTIONS, Permission.REPORTS,
            Permission.DASHBOARD_VOCATIONAL, Permission.DASHBOARD_OTM, Permission.DASHBOARD_SURVEYS)),
    COMBAT_TRAINING(EnumSet.of(Permission.RESULT_READ, Permission.REPORTS, Permission.DASHBOARD_VOCATIONAL)),
    EDUCATION(EnumSet.of(Permission.RESULT_READ, Permission.ADMISSION_READ, Permission.REPORTS,
            Permission.DASHBOARD_OTM)),
    LEADER(EnumSet.of(Permission.SOLDIER_READ)),
    PSYCHOLOGY(EnumSet.of(
            Permission.SOLDIER_READ, Permission.SOLDIER_WRITE,
            Permission.QUESTIONNAIRE_READ, Permission.QUESTIONNAIRE_WRITE,
            Permission.ATTACHMENT_WRITE, Permission.DASHBOARD_SURVEYS));

    private final Set<Permission> permissions;

    DemoUnitProfile(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    Set<Permission> permissions() {
        return permissions;
    }
}
