package uz.askar.education.soldiers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.common.PageResponse;
import uz.askar.education.security.Access;
import uz.askar.education.soldiers.SoldierDtos.SoldierDto;
import uz.askar.education.soldiers.SoldierDtos.SoldierRequest;
import uz.askar.education.soldiers.SoldierDtos.SoldierSummary;
import uz.askar.education.soldiers.SoldierDtos.SourceLookupResponse;

@RestController
@RequestMapping("/api/soldiers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Askarlar (yig'ma jild)", description = "M2: askar yig'ma jildi va JShShIR bo'yicha manba tizimdan olish")
public class SoldierController {

    private final SoldierService soldierService;
    private final SoldierTransferService transferService;
    private final SoldierSourceSyncService syncService;

    @GetMapping
    @PreAuthorize(Access.SOLDIER_READ)
    public PageResponse<SoldierSummary> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long subdivisionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return soldierService.search(query, unitId, subdivisionId, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Access.SOLDIER_READ)
    public SoldierDto get(@PathVariable Long id) {
        return soldierService.get(id);
    }

    @GetMapping("/source-lookup")
    @PreAuthorize(Access.SOLDIER_WRITE)
    public SourceLookupResponse lookup(
            @RequestParam @Pattern(regexp = SoldierDtos.PINFL_REGEX, message = "JShShIR 14 ta raqam bo'lishi kerak")
            String pinfl) {
        return soldierService.lookupInSource(pinfl);
    }

    @GetMapping("/{id}/transfers")
    @PreAuthorize(Access.SOLDIER_READ)
    public java.util.List<SoldierTransferService.TransferDto> transfers(@PathVariable Long id) {
        return transferService.history(id);
    }

    @PostMapping("/{id}/transfer")
    @PreAuthorize(Access.TRANSFER)
    public SoldierTransferService.TransferDto transfer(@PathVariable Long id,
            @Valid @RequestBody SoldierTransferService.TransferRequest request) {
        return transferService.transfer(id, request);
    }

    @PostMapping("/{id}/source-refresh")
    @PreAuthorize(Access.SOLDIER_WRITE)
    public java.util.List<SoldierSourceSyncService.FieldDiff> refreshDiff(@PathVariable Long id) {
        return syncService.diff(id);
    }

    @PutMapping("/{id}/source-refresh")
    @PreAuthorize(Access.SOLDIER_WRITE)
    public void applyRefresh(@PathVariable Long id, @RequestBody java.util.Set<String> fields) {
        syncService.apply(id, fields);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.SOLDIER_WRITE)
    public SoldierDto create(@Valid @RequestBody SoldierRequest request) {
        return soldierService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.SOLDIER_WRITE)
    public SoldierDto update(@PathVariable Long id, @Valid @RequestBody SoldierRequest request) {
        return soldierService.update(id, request);
    }
}
