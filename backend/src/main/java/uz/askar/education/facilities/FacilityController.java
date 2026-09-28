package uz.askar.education.facilities;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.facilities.FacilityService.FacilityDto;
import uz.askar.education.facilities.FacilityService.FacilityRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
@Tag(name = "O'quv-moddiy baza (xatlov)", description = "M4")
public class FacilityController {

    private final FacilityService service;

    @GetMapping
    @PreAuthorize(Access.GROUP_READ)
    public List<FacilityDto> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.GROUP_WRITE)
    public FacilityDto create(@Valid @RequestBody FacilityRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.GROUP_WRITE)
    public FacilityDto update(@PathVariable Long id, @Valid @RequestBody FacilityRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.GROUP_WRITE)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
