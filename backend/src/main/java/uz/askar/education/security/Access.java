package uz.askar.education.security;

/** {@code @PreAuthorize} ifodalari — qaysi rol qaysi amalni bajara olishi bir joyda turadi. */
public final class Access {

    private Access() {
    }

    public static final String ADMIN = "hasRole('SYSTEM_ADMIN')";

    public static final String DICTIONARY_WRITE = "hasAnyRole('SYSTEM_ADMIN','HKTB')";

    public static final String SOLDIER_WRITE =
            "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER','UNIT_OPERATOR','PSYCHOLOGIST')";

    public static final String SOLDIER_READ =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR',"
                    + "'PSYCHOLOGIST','GROUP_LEADER')";

    public static final String QUESTIONNAIRE_WRITE = "hasAnyRole('SYSTEM_ADMIN','PSYCHOLOGIST','UNIT_COMMANDER')";

    public static final String QUESTIONNAIRE_READ =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR','PSYCHOLOGIST')";

    public static final String ATTACHMENT_WRITE =
            "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER','UNIT_OPERATOR','PSYCHOLOGIST')";

    public static final String ASSIGNMENT_PROPOSE =
            "hasAnyRole('SYSTEM_ADMIN','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String ASSIGNMENT_DECIDE = "hasAnyRole('SYSTEM_ADMIN','HKTB')";

    public static final String ASSIGNMENT_READ =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String DEADLINE_MANAGE = "hasAnyRole('SYSTEM_ADMIN','HKTB')";

    public static final String RESULT_WRITE = "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String RESULT_READ =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR','COMBAT_TRAINING_DEPT','EDUCATION_DEPT')";

    public static final String ADMISSION_WRITE = "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String ADMISSION_READ =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','TMIBB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR','EDUCATION_DEPT')";

    public static final String EMPLOYMENT = "hasAnyRole('SYSTEM_ADMIN','HKTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String TRANSFER = "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER')";

    public static final String GROUP_WRITE = "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String GROUP_LEADER_ASSIGN = "hasAnyRole('SYSTEM_ADMIN','UNIT_COMMANDER')";

    public static final String GROUP_READ =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','JTB','TMIBB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR',"
                    + "'COMBAT_TRAINING_DEPT','EDUCATION_DEPT','GROUP_LEADER','PSYCHOLOGIST')";

    public static final String SCHEDULE_WRITE = "hasAnyRole('SYSTEM_ADMIN','JTB','UNIT_COMMANDER','UNIT_OPERATOR')";

    public static final String DASHBOARD_VOCATIONAL =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','JTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR',"
                    + "'COMBAT_TRAINING_DEPT')";

    public static final String DASHBOARD_OTM =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','TMIBB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR',"
                    + "'EDUCATION_DEPT')";

    public static final String DASHBOARD_SURVEYS =
            "hasAnyRole('SYSTEM_ADMIN','HKTB','DISTRICT_OFFICER','UNIT_COMMANDER','UNIT_OPERATOR','PSYCHOLOGIST')";
}
