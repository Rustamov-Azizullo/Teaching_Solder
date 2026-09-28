package uz.askar.education.surveys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.soldiers.Soldier;

/**
 * So'rovnoma natijalaridan guruhlarga taqsimot taklifi (TT M5): bir xil kasb yo'nalishini tanlaganlar bitta kasb
 * guruhiga, bir xil majburiy fanni belgilaganlar tegishli fan guruhiga. Yakuniy taqsimotni qism xodimi tasdiqlaydi.
 */
@Service
@RequiredArgsConstructor
public class GroupSuggestionService {

    public record SuggestedSoldier(Long id, String fullName) {
    }

    public record Suggestion(String kind, String label, List<SuggestedSoldier> soldiers) {
    }

    private final QuestionnaireRepository questionnaires;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<Suggestion> suggest(Long unitId) {
        var scope = currentUser.scope();
        Long unitFilter = scope.unitFilter() != null ? scope.unitFilter() : unitId;
        var finalized = questionnaires.findFinalizedInScope(java.time.LocalDate.now().getYear(),
                scope.districtFilter(), unitFilter);
        Map<String, List<SuggestedSoldier>> directions = new LinkedHashMap<>();
        Map<String, List<SuggestedSoldier>> subjects = new LinkedHashMap<>();
        for (Questionnaire q : finalized) {
            Soldier soldier = q.getSoldier();
            var ref = new SuggestedSoldier(soldier.getId(), soldier.getFullName());
            if (q.getInterestDirection() != null) {
                directions.computeIfAbsent(q.getInterestDirection().getName(), k -> new ArrayList<>()).add(ref);
            }
            q.getMandatorySubjects().forEach(subject ->
                    subjects.computeIfAbsent(subject.getName(), k -> new ArrayList<>()).add(ref));
        }
        List<Suggestion> result = new ArrayList<>();
        directions.forEach((label, list) -> result.add(new Suggestion("DIRECTION", label, list)));
        subjects.forEach((label, list) -> result.add(new Suggestion("SUBJECT", label, list)));
        result.sort(Comparator.comparing(Suggestion::kind).thenComparing(s -> -s.soldiers().size()));
        return result;
    }
}
