package uz.askar.education.organization;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.askar.education.organization.OrganizationDtos.MilitaryUnitDto;
import uz.askar.education.organization.OrganizationDtos.NamedRef;
import uz.askar.education.security.CurrentUser;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tashkiliy tuzilma", description = "Viloyat/tumanlar, harbiy okruglar va qismlar")
public class OrganizationController {

    private final RegionRepository regions;
    private final TerritorialDistrictRepository districts;
    private final MilitaryDistrictRepository militaryDistricts;
    private final MilitaryUnitRepository militaryUnits;
    private final CurrentUser currentUser;

    @GetMapping("/regions")
    public List<NamedRef> regions() {
        return regions.findAllByOrderByNameAsc().stream().map(r -> new NamedRef(r.getId(), r.getName())).toList();
    }

    @GetMapping("/regions/{regionId}/districts")
    public List<NamedRef> districts(@PathVariable Long regionId) {
        return districts.findByRegionIdOrderByNameAsc(regionId).stream()
                .map(d -> new NamedRef(d.getId(), d.getName())).toList();
    }

    /** Foydalanuvchining vakolat doirasidagi harbiy okruglar. */
    @GetMapping("/military-districts")
    @Transactional(readOnly = true)
    public List<NamedRef> militaryDistricts() {
        Long districtFilter = currentUser.scope().districtFilter();
        Long unitFilter = currentUser.scope().unitFilter();
        return militaryUnits.findInScope(districtFilter, unitFilter).stream()
                .map(unit -> unit.getMilitaryDistrict())
                .distinct()
                .map(d -> new NamedRef(d.getId(), d.getName()))
                .toList();
    }

    /** Foydalanuvchining vakolat doirasidagi harbiy qismlar. */
    @GetMapping("/military-units")
    @Transactional(readOnly = true)
    public List<MilitaryUnitDto> militaryUnits() {
        var scope = currentUser.scope();
        return militaryUnits.findInScope(scope.districtFilter(), scope.unitFilter()).stream()
                .map(MilitaryUnitDto::from).toList();
    }
}
