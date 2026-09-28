package uz.askar.education.surveys;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.security.Access;
import uz.askar.education.surveys.QuestionnaireDtos.QuestionnaireDto;
import uz.askar.education.surveys.QuestionnaireDtos.QuestionnaireRequest;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Anketa (so'rovnoma)", description = "M5: psixolog to'ldiradigan elektron anketa (IV–V bo'limlar)")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final GroupSuggestionService suggestionService;

    /** Joriy yillik sikl anketasi; hali to'ldirilmagan bo'lsa 204 qaytaradi. */
    @GetMapping("/soldiers/{soldierId}/questionnaire")
    @PreAuthorize(Access.QUESTIONNAIRE_READ)
    public ResponseEntity<QuestionnaireDto> get(@PathVariable Long soldierId) {
        return questionnaireService.find(soldierId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/soldiers/{soldierId}/questionnaire")
    @PreAuthorize(Access.QUESTIONNAIRE_WRITE)
    public QuestionnaireDto save(@PathVariable Long soldierId, @Valid @RequestBody QuestionnaireRequest request) {
        return questionnaireService.save(soldierId, request);
    }

    @GetMapping("/surveys/group-suggestions")
    @PreAuthorize(Access.GROUP_WRITE)
    public java.util.List<GroupSuggestionService.Suggestion> suggestions(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long unitId) {
        return suggestionService.suggest(unitId);
    }
}
