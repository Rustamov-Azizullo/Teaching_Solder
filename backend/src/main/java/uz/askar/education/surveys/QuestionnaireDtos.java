package uz.askar.education.surveys;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import uz.askar.education.organization.OrganizationDtos.NamedRef;

public final class QuestionnaireDtos {

    public static final int MAX_UNIVERSITY_CHOICES = 3;

    private QuestionnaireDtos() {
    }

    public record UniversityChoiceInput(
            @Min(1) @Max(MAX_UNIVERSITY_CHOICES) int priority,
            @NotBlank @Size(max = 255) String university,
            @NotBlank @Size(max = 255) String studyDirection) {
    }

    public record QuestionnaireRequest(
            Long interestDirectionId,
            @Size(max = 255) String interestOtherText,
            Set<Long> futurePlanIds,
            @Size(max = 255) String planOtherText,
            @Valid List<UniversityChoiceInput> universityChoices,
            Set<Long> specialtySubjectIds,
            Set<Long> mandatorySubjectIds,
            @Size(max = 255) String otherSubjectText,
            boolean complete) {
    }

    public record UniversityChoiceDto(int priority, String university, String studyDirection) {
    }

    public record QuestionnaireDto(
            Long id, Long soldierId, int cycleYear, QuestionnaireStatus status, String psychologistName,
            LocalDate filledDate, NamedRef interestDirection, String interestOtherText, List<NamedRef> futurePlans,
            String planOtherText, List<UniversityChoiceDto> universityChoices, List<NamedRef> specialtySubjects,
            List<NamedRef> mandatorySubjects, String otherSubjectText) {
    }
}
