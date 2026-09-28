package uz.askar.education.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;

/** Tizim sozlamalari (TT M14): anketa tanlov rejimi, davomatni tahrirlash muddati va h.k. */
@Service
@RequiredArgsConstructor
public class SettingsService {

    public static final String FUTURE_PLAN_MODE = "survey.futurePlan.mode";
    public static final String ATTENDANCE_EDIT_HOURS = "attendance.editHours";
    public static final String LESSON_DEFAULT_START = "lesson.defaultStart";
    public static final String LESSON_DEFAULT_END = "lesson.defaultEnd";
    public static final String LESSON_DEFAULT_HOURS = "lesson.defaultAcademicHours";

    private static final int DEFAULT_EDIT_HOURS = 24;

    private final AppSettingRepository settings;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public Map<String, String> all() {
        Map<String, String> result = new LinkedHashMap<>();
        settings.findAll().stream()
                .sorted(java.util.Comparator.comparing(AppSetting::getKey))
                .forEach(setting -> result.put(setting.getKey(), setting.getValue()));
        return result;
    }

    @Transactional(readOnly = true)
    public String get(String key, String fallback) {
        return settings.findById(key).map(AppSetting::getValue).orElse(fallback);
    }

    @Transactional(readOnly = true)
    public int attendanceEditHours() {
        try {
            return Integer.parseInt(get(ATTENDANCE_EDIT_HOURS, String.valueOf(DEFAULT_EDIT_HOURS)));
        } catch (NumberFormatException ex) {
            return DEFAULT_EDIT_HOURS;
        }
    }

    @Transactional
    public void update(Map<String, String> changes) {
        changes.forEach((key, value) -> {
            AppSetting setting = settings.findById(key)
                    .orElseThrow(() -> new BusinessRuleException("Noma'lum sozlama: " + key));
            setting.setValue(value);
            audit.record("UPDATE", "AppSetting", key, "qiymat=" + value);
        });
    }
}
