package uz.askar.education.cycles;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.cycles.CycleService.CycleDto;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
@Tag(name = "Yillik sikllar")
public class CycleController {

    private final CycleService service;

    @GetMapping
    public List<CycleDto> list() {
        return service.list();
    }

    @PostMapping("/{year}/open")
    @PreAuthorize(Access.ADMIN)
    public CycleDto open(@PathVariable int year) {
        return service.openNew(year);
    }

    @PostMapping("/{year}/close")
    @PreAuthorize(Access.ADMIN)
    public CycleDto close(@PathVariable int year) {
        return service.close(year);
    }
}
