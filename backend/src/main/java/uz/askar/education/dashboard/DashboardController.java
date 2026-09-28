package uz.askar.education.dashboard;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.dashboard.DashboardDtos.AttendanceBlock;
import uz.askar.education.dashboard.DashboardDtos.SurveyBlock;
import uz.askar.education.groups.GroupType;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "M12: yo'nalishlar bo'yicha alohida bloklar")
public class DashboardController {

    private static final int DEFAULT_PERIOD_DAYS = 30;
    private static final int MAX_PERIOD_DAYS = 366;

    private final DashboardService dashboardService;

    @GetMapping("/vocational")
    @PreAuthorize(Access.DASHBOARD_VOCATIONAL)
    public AttendanceBlock vocational(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long unitId) {
        return attendance(GroupType.VOCATIONAL, from, to, districtId, unitId);
    }

    @GetMapping("/vocational/results")
    @PreAuthorize(Access.DASHBOARD_VOCATIONAL)
    public java.util.List<DashboardDtos.CountItem> courseResults(@RequestParam(required = false) Long districtId,
                                                                 @RequestParam(required = false) Long unitId) {
        return dashboardService.courseResults(districtId, unitId);
    }

    @GetMapping("/otm")
    @PreAuthorize(Access.DASHBOARD_OTM)
    public AttendanceBlock otm(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long unitId) {
        return attendance(GroupType.OTM_PREP, from, to, districtId, unitId);
    }

    @GetMapping("/surveys")
    @PreAuthorize(Access.DASHBOARD_SURVEYS)
    public SurveyBlock surveys(@RequestParam(required = false) Long districtId,
                               @RequestParam(required = false) Long unitId) {
        return dashboardService.surveyBlock(districtId, unitId);
    }

    private AttendanceBlock attendance(GroupType type, LocalDate from, LocalDate to, Long districtId, Long unitId) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_PERIOD_DAYS);
        if (start.isAfter(end) || start.plusDays(MAX_PERIOD_DAYS).isBefore(end)) {
            throw new BusinessRuleException("Davr noto'g'ri: boshlanish sanasi tugashdan keyin yoki davr 1 yildan uzun");
        }
        return dashboardService.attendanceBlock(type, start, end, districtId, unitId);
    }
}
