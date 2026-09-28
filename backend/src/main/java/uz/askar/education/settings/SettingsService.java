package uz.askar.education.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;

/** Tizim sozlamalari (TT M14): anketa tanlov rejimi va h.k. */
@Service
@RequiredArgsConstructor
public class SettingsService {

    public static final String FUTURE_PLAN_MODE = "survey.futurePlan.mode";
    public static final String LESSON_DEFAULT_START = "lesson.defaultStart";
    public static final String LESSON_DEFAULT_END = "lesson.defaultEnd";
    public static final String LESSON_DEFAULT_HOURS = "lesson.defaultAcademicHours";

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
