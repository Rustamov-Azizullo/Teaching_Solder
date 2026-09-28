package uz.askar.education.facilities;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.OrganizationDtos.NamedRef;
import uz.askar.education.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class FacilityService {

    public record FacilityRequest(
            @NotNull Long militaryUnitId,
            @NotBlank @Size(max = 160) String name,
            @NotNull FacilityKind kind,
            @Min(0) Integer capacity,
            @NotNull FacilityCondition condition,
            @Size(max = 500) String equipment,
            @Size(max = 500) String shortages,
            LocalDate surveyDate,
            Set<Long> suitableForIds) {
    }

    public record FacilityDto(Long id, Long militaryUnitId, String militaryUnitName, String name, FacilityKind kind,
                              Integer capacity, FacilityCondition condition, String equipment, String shortages,
                              LocalDate surveyDate, List<NamedRef> suitableFor) {
    }

    private final FacilityRepository facilities;
    private final MilitaryUnitRepository militaryUnits;
    private final DictionaryItemRepository dictionaryItems;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<FacilityDto> list() {
        var scope = currentUser.scope();
        return facilities.findInScope(scope.districtFilter(), scope.unitFilter()).stream().map(this::toDto).toList();
    }

    @Transactional
    public FacilityDto create(FacilityRequest request) {
        Facility facility = new Facility();
        apply(facility, request);
        Facility saved = facilities.save(facility);
        audit.record("CREATE", "Facility", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Transactional
    public FacilityDto update(Long id, FacilityRequest request) {
        Facility facility = findInScope(id);
        apply(facility, request);
        audit.record("UPDATE", "Facility", id, facility.getName());
        return toDto(facility);
    }

    @Transactional
    public void delete(Long id) {
        Facility facility = findInScope(id);
        facilities.delete(facility);
        audit.record("DELETE", "Facility", id, facility.getName());
    }

    public Facility findInScope(Long id) {
        Facility facility = facilities.findById(id).orElseThrow(() -> new NotFoundException("Xatlov yozuvi topilmadi"));
        currentUser.scope().require(facility.getMilitaryUnit().getMilitaryDistrict().getId(),
                facility.getMilitaryUnit().getId());
        return facility;
    }

    private void apply(Facility facility, FacilityRequest request) {
        var unit = militaryUnits.findById(request.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        facility.setMilitaryUnit(unit);
        facility.setName(request.name());
        facility.setKind(request.kind());
        facility.setCapacity(request.capacity());
        facility.setCondition(request.condition());
        facility.setEquipment(request.equipment());
        facility.setShortages(request.shortages());
        facility.setSurveyDate(request.surveyDate());
        Set<Long> ids = request.suitableForIds() == null ? Set.of() : request.suitableForIds();
        List<DictionaryItem> found = dictionaryItems.findByIdIn(ids);
        boolean valid = found.size() == new HashSet<>(ids).size() && found.stream().allMatch(
                item -> item.getType() == DictionaryType.PROFESSION || item.getType() == DictionaryType.SUBJECT);
        if (!valid) {
            throw new BusinessRuleException("Yaroqlilik ro'yxatida faqat kasb va fanlar bo'lishi mumkin");
        }
        facility.setSuitableFor(new HashSet<>(found));
    }

    private FacilityDto toDto(Facility f) {
        return new FacilityDto(f.getId(), f.getMilitaryUnit().getId(), f.getMilitaryUnit().getName(), f.getName(),
                f.getKind(), f.getCapacity(), f.getCondition(), f.getEquipment(), f.getShortages(), f.getSurveyDate(),
                f.getSuitableFor().stream().map(i -> new NamedRef(i.getId(), i.getName())).toList());
    }
}
