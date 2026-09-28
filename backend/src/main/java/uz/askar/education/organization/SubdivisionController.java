package uz.askar.education.organization;

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
import uz.askar.education.organization.SubdivisionService.SubdivisionNode;
import uz.askar.education.organization.SubdivisionService.SubdivisionRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Bo'linmalar", description = "Harbiy qism ichidagi ierarxik bo'linmalar")
public class SubdivisionController {

    private final SubdivisionService service;

    @GetMapping("/military-units/{unitId}/subdivisions")
    public List<SubdivisionNode> tree(@PathVariable Long unitId) {
        return service.tree(unitId);
    }

    @PostMapping("/military-units/{unitId}/subdivisions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.GROUP_WRITE)
    public SubdivisionNode create(@PathVariable Long unitId, @Valid @RequestBody SubdivisionRequest request) {
        return service.create(unitId, request);
    }

    @PutMapping("/subdivisions/{id}")
    @PreAuthorize(Access.GROUP_WRITE)
    public SubdivisionNode update(@PathVariable Long id, @Valid @RequestBody SubdivisionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/subdivisions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.GROUP_WRITE)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
