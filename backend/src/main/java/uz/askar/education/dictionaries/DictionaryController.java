package uz.askar.education.dictionaries;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.dictionaries.DictionaryDtos.DictionaryItemDto;
import uz.askar.education.dictionaries.DictionaryDtos.DictionaryItemRequest;
import uz.askar.education.dictionaries.DictionaryDtos.DictionaryTypeDto;
import uz.askar.education.dictionaries.DictionaryDtos.UnitDirectionsRequest;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.security.Access;
import uz.askar.education.security.CurrentUser;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Ma'lumotnomalar", description = "Dinamik ma'lumotnomalar: fanlar, kasblar, kasb yo'nalishlari va boshqalar")
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final MilitaryUnitRepository militaryUnits;
    private final CurrentUser currentUser;

    @GetMapping("/dictionaries")
    public List<DictionaryTypeDto> types() {
        return Arrays.stream(DictionaryType.values())
                .map(type -> new DictionaryTypeDto(type.name(), type.label())).toList();
    }

    @GetMapping("/dictionaries/{type}")
    public List<DictionaryItemDto> list(
            @PathVariable DictionaryType type,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(required = false) Long unitId) {
        return dictionaryService.list(type, activeOnly, unitId);
    }

    @PostMapping("/dictionaries/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Access.DICTIONARY_WRITE)
    public DictionaryItemDto create(@PathVariable DictionaryType type,
                                    @Valid @RequestBody DictionaryItemRequest request) {
        return dictionaryService.create(type, request);
    }

    @PutMapping("/dictionaries/{type}/{id}")
    @PreAuthorize(Access.DICTIONARY_WRITE)
    public DictionaryItemDto update(@PathVariable DictionaryType type, @PathVariable Long id,
                                    @Valid @RequestBody DictionaryItemRequest request) {
        return dictionaryService.update(type, id, request);
    }

    @DeleteMapping("/dictionaries/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Access.DICTIONARY_WRITE)
    public void delete(@PathVariable DictionaryType type, @PathVariable Long id) {
        dictionaryService.delete(type, id);
    }

    @GetMapping("/military-units/{unitId}/directions")
    public List<Long> unitDirections(@PathVariable Long unitId) {
        return dictionaryService.unitDirectionIds(unitId);
    }

    @PutMapping("/military-units/{unitId}/directions")
    @PreAuthorize(Access.UNIT_DIRECTIONS)
    @Transactional
    public void replaceUnitDirections(@PathVariable Long unitId, @RequestBody UnitDirectionsRequest request) {
        var unit = militaryUnits.findById(unitId).orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        dictionaryService.replaceUnitDirections(currentUser.scope(), unit.getMilitaryDistrict().getId(), unitId,
                request.directionIds());
    }
}
