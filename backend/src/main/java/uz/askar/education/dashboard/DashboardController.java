package uz.askar.education.dashboard;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.dashboard.DashboardDtos.SurveyBlock;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "M12: yo'nalishlar bo'yicha alohida bloklar")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/vocational/results")
    @PreAuthorize(Access.DASHBOARD_VOCATIONAL)
    public java.util.List<DashboardDtos.CountItem> courseResults(@RequestParam(required = false) Long districtId,
                                                                 @RequestParam(required = false) Long unitId) {
        return dashboardService.courseResults(districtId, unitId);
    }

    @GetMapping("/surveys")
    @PreAuthorize(Access.DASHBOARD_SURVEYS)
    public SurveyBlock surveys(@RequestParam(required = false) Long districtId,
                               @RequestParam(required = false) Long unitId) {
        return dashboardService.surveyBlock(districtId, unitId);
    }
}
