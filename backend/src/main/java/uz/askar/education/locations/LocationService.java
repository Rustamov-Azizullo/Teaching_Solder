package uz.askar.education.locations;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class LocationService {

    public record LocationDto(Long id, Long parentId, LocationLevel level, String name, String code,
                              Long militaryDistrictId, Long militaryUnitId) {

        static LocationDto from(Location location) {
            return new LocationDto(location.getId(),
                    location.getParent() != null ? location.getParent().getId() : null,
                    location.getLevel(), location.getName(), location.getCode(),
                    location.getMilitaryDistrict() != null ? location.getMilitaryDistrict().getId() : null,
                    location.getMilitaryUnit() != null ? location.getMilitaryUnit().getId() : null);
        }
    }

    private final LocationRepository locations;
    private final CurrentUser currentUser;

    /** Joriy foydalanuvchining vakolat doirasidagi hududlar (tekis ro'yxat, {@code parentId} bilan). */
    @Transactional(readOnly = true)
    public List<LocationDto> listInScope() {
        return locations.findInDistrictScope(currentUser.scope().districtFilter()).stream()
                .map(LocationDto::from).toList();
    }

    /** Harbiy okrug uchun hudud yozuvini qaytaradi, bo'lmasa yaratadi (respublika ildizi ostida). */
    @Transactional
    public Location ensureForDistrict(MilitaryDistrict district) {
        return locations.findByMilitaryDistrictId(district.getId()).orElseGet(() -> {
            Location location = new Location();
            location.setParent(republic());
            location.setLevel(LocationLevel.DISTRICT);
            location.setName(district.getName());
            location.setCode(district.getCode());
            location.setMilitaryDistrict(district);
            return locations.save(location);
        });
    }

    /** Harbiy qism uchun hudud yozuvini qaytaradi, bo'lmasa yaratadi (qism okrugining hududi ostida). */
    @Transactional
    public Location ensureForUnit(MilitaryUnit unit) {
        return locations.findByMilitaryUnitId(unit.getId()).orElseGet(() -> {
            Location location = new Location();
            location.setParent(ensureForDistrict(unit.getMilitaryDistrict()));
            location.setLevel(LocationLevel.UNIT);
            location.setName(unit.getName());
            location.setCode(unit.getCode());
            location.setMilitaryUnit(unit);
            return locations.save(location);
        });
    }

    private Location republic() {
        return locations.findFirstByLevel(LocationLevel.REPUBLIC)
                .orElseThrow(() -> new NotFoundException("Respublika hududi topilmadi"));
    }
}
