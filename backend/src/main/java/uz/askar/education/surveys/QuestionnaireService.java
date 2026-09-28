package uz.askar.education.surveys;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.organization.OrganizationDtos.NamedRef;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.settings.SettingsService;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierService;
import uz.askar.education.surveys.QuestionnaireDtos.QuestionnaireDto;
import uz.askar.education.surveys.QuestionnaireDtos.QuestionnaireRequest;
import uz.askar.education.surveys.QuestionnaireDtos.UniversityChoiceDto;
import uz.askar.education.surveys.QuestionnaireDtos.UniversityChoiceInput;
import uz.askar.education.users.AppUserRepository;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    static final String OTHER_CODE = "OTHER";
    static final String OTM_ADMISSION_CODE = "OTM_ADMISSION";
    private static final String SINGLE_MODE = "SINGLE";

    private final QuestionnaireRepository questionnaires;
    private final SoldierService soldierService;
    private final DictionaryItemRepository dictionaryItems;
    private final AppUserRepository users;
    private final SettingsService settings;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public Optional<QuestionnaireDto> find(Long soldierId) {
        soldierService.findInScope(soldierId);
        return questionnaires.findBySoldierIdAndCycleYear(soldierId, currentCycleYear()).map(this::toDto);
    }

    @Transactional
    public QuestionnaireDto save(Long soldierId, QuestionnaireRequest request) {
        Soldier soldier = soldierService.findInScope(soldierId);
        Questionnaire questionnaire = questionnaires.findBySoldierIdAndCycleYear(soldierId, currentCycleYear())
                .orElseGet(() -> newQuestionnaire(soldier));
        boolean wasFinalized = questionnaire.isFinalized();

        apply(questionnaire, request);
        if (request.complete()) {
            validateForFinalization(questionnaire, soldier);
            questionnaire.setStatus(QuestionnaireStatus.FINALIZED);
        }
        questionnaire.setUpdatedAt(LocalDateTime.now());
        Questionnaire saved = questionnaires.save(questionnaire);
        audit.record(wasFinalized ? "UPDATE_FINALIZED" : "SAVE", "Questionnaire", saved.getId(),
                "askar=" + soldierId + ", holat=" + saved.getStatus());
        return toDto(saved);
    }

    private Questionnaire newQuestionnaire(Soldier soldier) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setSoldier(soldier);
        questionnaire.setCycleYear(currentCycleYear());
        questionnaire.setFilledDate(LocalDate.now());
        questionnaire.setPsychologist(users.findById(currentUser.id())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi")));
        return questionnaire;
    }

    private void apply(Questionnaire target, QuestionnaireRequest request) {
        target.setInterestDirection(request.interestDirectionId() == null ? null
                : item(request.interestDirectionId(), DictionaryType.PROFESSION_DIRECTION));
        target.setInterestOtherText(request.interestOtherText());
        target.setPlanOtherText(request.planOtherText());
        target.setOtherSubjectText(request.otherSubjectText());
        target.setFuturePlans(items(request.futurePlanIds(), DictionaryType.FUTURE_PLAN));
        target.setSpecialtySubjects(items(request.specialtySubjectIds(), DictionaryType.SUBJECT));
        target.setMandatorySubjects(items(request.mandatorySubjectIds(), DictionaryType.SUBJECT));
        replaceUniversityChoices(target, request.universityChoices());
    }

    private void replaceUniversityChoices(Questionnaire target, List<UniversityChoiceInput> inputs) {
        target.getUniversityChoices().clear();
        if (inputs == null) {
            return;
        }
        Set<Integer> priorities = new HashSet<>();
        for (UniversityChoiceInput input : inputs) {
            if (!priorities.add(input.priority())) {
                throw new BusinessRuleException("Ustuvorlik raqamlari takrorlanmasligi kerak");
            }
            UniversityChoice choice = new UniversityChoice();
            choice.setQuestionnaire(target);
            choice.setPriority(input.priority());
            choice.setUniversity(input.university());
            choice.setStudyDirection(input.studyDirection());
            target.getUniversityChoices().add(choice);
        }
    }

    /** TT 6.1: "bandlarni to'liq to'ldiring"; V bo'lim faqat "oliy ta'limga kirish" tanlanganda majburiy. */
    private void validateForFinalization(Questionnaire q, Soldier soldier) {
        if (q.getInterestDirection() == null) {
            throw new BusinessRuleException("Qiziqadigan kasb yo'nalishi tanlanmagan (14-savol)");
        }
        if (isOther(q.getInterestDirection()) && isBlank(q.getInterestOtherText())) {
            throw new BusinessRuleException("\"Boshqa\" yo'nalish uchun matn kiritilishi shart (14-savol)");
        }
        if (q.getFuturePlans().isEmpty()) {
            throw new BusinessRuleException("Kelgusi reja tanlanmagan (15-savol)");
        }
        if (SINGLE_MODE.equals(settings.get(SettingsService.FUTURE_PLAN_MODE, "MULTIPLE"))
                && q.getFuturePlans().size() > 1) {
            throw new BusinessRuleException("15-savolda faqat bitta variant tanlanadi");
        }
        if (q.getFuturePlans().stream().anyMatch(this::isOther) && isBlank(q.getPlanOtherText())) {
            throw new BusinessRuleException("\"Boshqa\" reja uchun matn kiritilishi shart (15-savol)");
        }
        if (q.getFuturePlans().stream().anyMatch(plan -> OTM_ADMISSION_CODE.equals(plan.getCode()))) {
            validateHigherEducationSection(q);
        }
        if (!soldier.getMilitaryUnit().getId().equals(q.getSoldier().getMilitaryUnit().getId())) {
            throw new BusinessRuleException("Askar boshqa qismga tegishli");
        }
    }

    private void validateHigherEducationSection(Questionnaire q) {
        if (q.getUniversityChoices().isEmpty()) {
            throw new BusinessRuleException("OTM va ta'lim yo'nalishi kiritilmagan (16-savol)");
        }
        if (q.getSpecialtySubjects().isEmpty()) {
            throw new BusinessRuleException("Ixtisoslik fanlari tanlanmagan (17-savol)");
        }
        if (q.getMandatorySubjects().isEmpty()) {
            throw new BusinessRuleException("Majburiy fanlardan qo'shimcha tayyorgarlik tanlanmagan (18-savol)");
        }
    }

    private Set<DictionaryItem> items(Collection<Long> ids, DictionaryType type) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<DictionaryItem> found = dictionaryItems.findByIdIn(ids);
        boolean valid = found.size() == new HashSet<>(ids).size()
                && found.stream().allMatch(item -> item.getType() == type);
        if (!valid) {
            throw new BusinessRuleException("Ma'lumotnoma yozuvlari noto'g'ri: " + type);
        }
        return new HashSet<>(found);
    }

    private DictionaryItem item(Long id, DictionaryType type) {
        return dictionaryItems.findById(id).filter(item -> item.getType() == type)
                .orElseThrow(() -> new NotFoundException("Ma'lumotnoma yozuvi topilmadi: " + type));
    }

    private boolean isOther(DictionaryItem item) {
        return OTHER_CODE.equals(item.getCode());
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private int currentCycleYear() {
        return LocalDate.now().getYear();
    }

    private QuestionnaireDto toDto(Questionnaire q) {
        return new QuestionnaireDto(q.getId(), q.getSoldier().getId(), q.getCycleYear(), q.getStatus(),
                q.getPsychologist().getFullName(), q.getFilledDate(),
                q.getInterestDirection() == null ? null : ref(q.getInterestDirection()), q.getInterestOtherText(),
                refs(q.getFuturePlans()), q.getPlanOtherText(),
                q.getUniversityChoices().stream().sorted(Comparator.comparingInt(UniversityChoice::getPriority))
                        .map(c -> new UniversityChoiceDto(c.getPriority(), c.getUniversity(), c.getStudyDirection()))
                        .toList(),
                refs(q.getSpecialtySubjects()), refs(q.getMandatorySubjects()), q.getOtherSubjectText());
    }

    private List<NamedRef> refs(Collection<DictionaryItem> items) {
        return items.stream().sorted(Comparator.comparing(DictionaryItem::getSortOrder)).map(this::ref).toList();
    }

    private NamedRef ref(DictionaryItem item) {
        return new NamedRef(item.getId(), item.getName());
    }
}
