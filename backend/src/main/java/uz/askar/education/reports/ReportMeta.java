package uz.askar.education.reports;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.askar.education.security.CurrentUser;

/** Hisobot sarlavhasi ostidagi yagona uslubdagi ma'lumot qatorlari: davr, vakolat doirasi, sana, tuzuvchi. */
@Component
@RequiredArgsConstructor
class ReportMeta {

    private final CurrentUser currentUser;

    /** {@code selection} — tanlangan ob'ekt tavsifi ("bo'linma: ..."), tanlanmagan bo'lsa {@code null}. */
    List<String> lines(String period, String selection) {
        var scope = currentUser.scope();
        String area = switch (scope.level()) {
            case REPUBLIC -> "Vazirlik";
            case DISTRICT -> "Harbiy okrug #" + scope.districtId();
            case UNIT -> "Harbiy qism #" + scope.unitId();
        };
        if (selection != null) {
            area = area + ", " + selection;
        }
        return List.of(period, "Vakolat doirasi: " + area, "Tuzilgan sana: " + LocalDate.now(),
                "Tuzuvchi: " + currentUser.username());
    }
}
