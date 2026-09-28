package uz.askar.education.assignments;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.assignments.AssignmentService.AssignmentDto;
import uz.askar.education.assignments.AssignmentService.ContractRequest;
import uz.askar.education.assignments.AssignmentService.DecisionRequest;
import uz.askar.education.assignments.AssignmentService.ProposalRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Biriktirishlar va shartnomalar", description = "M3")
public class AssignmentController {

    private final AssignmentService service;

    @GetMapping
    @PreAuthorize(Access.ASSIGNMENT_READ)
    public List<AssignmentDto> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.ASSIGNMENT_PROPOSE)
    public AssignmentDto propose(@Valid @RequestBody ProposalRequest request) {
        return service.propose(request);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize(Access.ASSIGNMENT_DECIDE)
    public AssignmentDto review(@PathVariable Long id) {
        return service.startReview(id);
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize(Access.ASSIGNMENT_DECIDE)
    public AssignmentDto decide(@PathVariable Long id, @Valid @RequestBody DecisionRequest request) {
        return service.decide(id, request);
    }

    @PutMapping("/{id}/contract")
    @PreAuthorize(Access.GROUP_WRITE)
    public AssignmentDto contract(@PathVariable Long id, @Valid @RequestBody ContractRequest request) {
        return service.updateContract(id, request);
    }
}
