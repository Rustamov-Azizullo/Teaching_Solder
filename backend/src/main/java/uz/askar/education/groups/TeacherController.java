package uz.askar.education.groups;

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
import uz.askar.education.groups.GroupDtos.InstitutionDto;
import uz.askar.education.groups.GroupDtos.InstitutionRequest;
import uz.askar.education.groups.GroupDtos.TeacherDto;
import uz.askar.education.groups.GroupDtos.TeacherRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "O'qituvchilar va muassasalar", description = "O'qituvchilar — faqat ma'lumot yozuvi (tizimga kirmaydi)")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/institutions")
    public List<InstitutionDto> institutions() {
        return teacherService.listInstitutions();
    }

    @PostMapping("/institutions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.GROUP_WRITE)
    public InstitutionDto createInstitution(@Valid @RequestBody InstitutionRequest request) {
        return teacherService.createInstitution(request);
    }

    @GetMapping("/teachers")
    @PreAuthorize(Access.GROUP_READ)
    public List<TeacherDto> teachers() {
        return teacherService.list();
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.GROUP_WRITE)
    public TeacherDto create(@Valid @RequestBody TeacherRequest request) {
        return teacherService.create(request);
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize(Access.GROUP_WRITE)
    public TeacherDto update(@PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return teacherService.update(id, request);
    }
}
