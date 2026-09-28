package uz.askar.education.admissions;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.admissions.AdmissionService.AdmissionRow;
import uz.askar.education.admissions.AdmissionService.AdmissionUpdate;
import uz.askar.education.admissions.AdmissionService.FunnelStep;
import uz.askar.education.admissions.AdmissionService.SyncResult;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/admissions")
@RequiredArgsConstructor
@Tag(name = "OTMga qabul", description = "M10")
public class AdmissionController {

    private final AdmissionService service;

    @GetMapping
    @PreAuthorize(Access.ADMISSION_READ)
    public List<AdmissionRow> list() {
        return service.list();
    }

    @PutMapping("/{soldierId}")
    @PreAuthorize(Access.ADMISSION_WRITE)
    public AdmissionRow update(@PathVariable Long soldierId, @Valid @RequestBody AdmissionUpdate update) {
        return service.update(soldierId, update);
    }

    @PostMapping("/bmba-sync")
    @PreAuthorize(Access.ADMISSION_WRITE)
    public SyncResult sync() {
        return service.syncFromBmba();
    }

    @GetMapping("/funnel")
    @PreAuthorize(Access.DASHBOARD_OTM)
    public List<FunnelStep> funnel() {
        return service.funnel();
    }

    @GetMapping("/reserve-list")
    @PreAuthorize(Access.ADMISSION_READ)
    public List<AdmissionRow> reserveList() {
        return service.reserveList(LocalDate.now());
    }
}
