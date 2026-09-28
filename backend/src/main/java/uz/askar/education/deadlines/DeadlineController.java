package uz.askar.education.deadlines;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.deadlines.DeadlineService.DeadlineDto;
import uz.askar.education.deadlines.DeadlineService.DeadlineRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
@Tag(name = "Muddatlar nazorati", description = "M13")
public class DeadlineController {

    private final DeadlineService service;

    @GetMapping
    public List<DeadlineDto> list(@RequestParam(required = false) Integer year) {
        return service.list(year == null ? LocalDate.now().getYear() : year);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.DEADLINE_MANAGE)
    public DeadlineDto create(@Valid @RequestBody DeadlineRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Access.DEADLINE_MANAGE)
    public DeadlineDto update(@PathVariable Long id, @Valid @RequestBody DeadlineRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize(Access.DEADLINE_MANAGE)
    public DeadlineDto complete(@PathVariable Long id) {
        return service.complete(id);
    }

    @PostMapping("/process")
    @PreAuthorize(Access.ADMIN)
    public Map<String, Integer> process() {
        return Map.of("sent", service.process(LocalDate.now()));
    }
}
