package uz.askar.education.results;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.results.ResultService.ResultsRequest;
import uz.askar.education.results.ResultService.ResultsSheet;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/groups/{groupId}/results")
@RequiredArgsConstructor
@Tag(name = "Kurs natijalari va sertifikatlar", description = "M9")
public class ResultController {

    private final ResultService service;

    @GetMapping
    @PreAuthorize(Access.RESULT_READ)
    public ResultsSheet sheet(@PathVariable Long groupId) {
        return service.sheet(groupId);
    }

    @PutMapping
    @PreAuthorize(Access.RESULT_WRITE)
    public ResultsSheet save(@PathVariable Long groupId, @Valid @RequestBody ResultsRequest request) {
        return service.save(groupId, request);
    }

    @PostMapping("/approve")
    @PreAuthorize(Access.GROUP_LEADER_ASSIGN)
    public ResultsSheet approve(@PathVariable Long groupId) {
        return service.approve(groupId);
    }
}
