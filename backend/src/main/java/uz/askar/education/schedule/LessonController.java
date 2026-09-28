package uz.askar.education.schedule;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import uz.askar.education.schedule.LessonDtos.CancelRequest;
import uz.askar.education.schedule.LessonDtos.GenerateLessonsRequest;
import uz.askar.education.schedule.LessonDtos.LessonDto;
import uz.askar.education.schedule.LessonDtos.LessonRequest;
import uz.askar.education.security.Access;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Dars jadvali", description = "M7: mashg'ulotlar jadvali (standart 15:00–17:25, 3 o'quv soati)")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/groups/{groupId}/lessons")
    @PreAuthorize(Access.GROUP_READ)
    public List<LessonDto> forGroup(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return lessonService.listForGroup(groupId, from, to);
    }

    /** Berilgan kundagi (vakolat doirasidagi) mashg'ulotlar — guruh kattasining "bugungi mashg'ulotlari". */
    @GetMapping("/lessons")
    @PreAuthorize(Access.GROUP_READ)
    public List<LessonDto> forDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return lessonService.listForDate(date);
    }

    @PostMapping("/groups/{groupId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.SCHEDULE_WRITE)
    public LessonDto create(@PathVariable Long groupId, @Valid @RequestBody LessonRequest request) {
        return lessonService.create(groupId, request);
    }

    @PostMapping("/groups/{groupId}/lessons/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.SCHEDULE_WRITE)
    public List<LessonDto> generate(@PathVariable Long groupId, @Valid @RequestBody GenerateLessonsRequest request) {
        return lessonService.generate(groupId, request);
    }

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize(Access.SCHEDULE_WRITE)
    public LessonDto update(@PathVariable Long lessonId, @Valid @RequestBody LessonRequest request) {
        return lessonService.update(lessonId, request);
    }

    @PostMapping("/lessons/{lessonId}/cancel")
    @PreAuthorize(Access.SCHEDULE_WRITE)
    public LessonDto cancel(@PathVariable Long lessonId, @Valid @RequestBody CancelRequest request) {
        return lessonService.cancel(lessonId, request);
    }
}
