package uz.askar.education.locations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BadRequestException;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryDistrictRepository;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
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

    public record LocationRequest(@NotBlank @Size(max = 255) String name, @Size(max = 60) String code,
                                  LocationLevel level, Long parentId) {
    }

    private final LocationRepository locations;
    private final MilitaryDistrictRepository militaryDistricts;
    private final MilitaryUnitRepository militaryUnits;
    private final AuditService audit;
    private final CurrentUser currentUser;

    /** Joriy foydalanuvchining vakolat doirasidagi hududlar (tekis ro'yxat, {@code parentId} bilan). */
    @Transactional(readOnly = true)
    public List<LocationDto> listInScope() {
        return locations.findInDistrictScope(currentUser.scope().districtFilter()).stream()
                .map(LocationDto::from).toList();
    }

    /** Yangi okrug (respublika ostida) yoki qism (okrug ostida) qo'shadi; mos harbiy okrug/qism yozuvi ham yaratiladi. */
    @Transactional
    public LocationDto create(LocationRequest request) {
        LocationLevel level = request.level();
        if (level == null || level == LocationLevel.REPUBLIC) {
            throw new BadRequestException("Faqat okrug yoki qism qo'shish mumkin");
        }
        Location parent = locations.findById(request.parentId() == null ? -1L : request.parentId())
                .orElseThrow(() -> new NotFoundException("Yuqori hudud topilmadi"));
        LocationLevel expectedParent = level == LocationLevel.DISTRICT ? LocationLevel.REPUBLIC : LocationLevel.DISTRICT;
        if (parent.getLevel() != expectedParent) {
            throw new BadRequestException(level == LocationLevel.DISTRICT
                    ? "Okrug respublika ostida bo'lishi kerak" : "Qism okrug ostida bo'lishi kerak");
        }
        Location location = new Location();
        location.setParent(parent);
        location.setLevel(level);
        location.setName(request.name().trim());
        location.setCode(blankToNull(request.code()));
        if (level == LocationLevel.DISTRICT) {
            MilitaryDistrict district = new MilitaryDistrict();
            district.setName(location.getName());
            district.setCode(location.getCode());
            location.setMilitaryDistrict(militaryDistricts.save(district));
        } else {
            requireFreeUnitCode(location.getCode(), null);
            MilitaryUnit unit = new MilitaryUnit();
            unit.setMilitaryDistrict(parent.getMilitaryDistrict());
            unit.setName(location.getName());
            unit.setCode(location.getCode());
            location.setMilitaryUnit(militaryUnits.save(unit));
        }
        Location saved = locations.save(location);
        audit.record("CREATE", "Location", saved.getId(), level + ": " + saved.getName());
        return LocationDto.from(saved);
    }

    /** Nomi va kodini o'zgartiradi (mos harbiy okrug/qism yozuvi bilan sinxron). Daraja va ota hudud o'zgarmaydi. */
    @Transactional
    public LocationDto update(Long id, LocationRequest request) {
        Location location = find(id);
        if (location.getLevel() == LocationLevel.UNIT) {
            requireFreeUnitCode(blankToNull(request.code()), location.getMilitaryUnit().getId());
        }
        location.setName(request.name().trim());
        location.setCode(blankToNull(request.code()));
        if (location.getMilitaryDistrict() != null) {
            location.getMilitaryDistrict().setName(location.getName());
            location.getMilitaryDistrict().setCode(location.getCode());
        }
        if (location.getMilitaryUnit() != null) {
            location.getMilitaryUnit().setName(location.getName());
            location.getMilitaryUnit().setCode(location.getCode());
        }
        audit.record("UPDATE", "Location", id, location.getLevel() + ": " + location.getName());
        return LocationDto.from(location);
    }

    /** Hududni o'chiradi: respublika, ichki hududlari yoki bog'liq ma'lumotlari (foydalanuvchi, askar, guruh) bor hudud o'chirilmaydi. */
    @Transactional
    public void delete(Long id) {
        Location location = find(id);
        if (location.getLevel() == LocationLevel.REPUBLIC) {
            throw new BusinessRuleException("Respublika ildizini o'chirib bo'lmaydi");
        }
        if (locations.existsByParentId(id)) {
            throw new BusinessRuleException("Avval ichidagi hududlarni (qismlarni) o'chiring");
        }
        String description = location.getLevel() + ": " + location.getName();
        try {
            locations.delete(location);
            locations.flush();
            if (location.getMilitaryUnit() != null) {
                militaryUnits.delete(location.getMilitaryUnit());
            }
            if (location.getMilitaryDistrict() != null) {
                militaryDistricts.delete(location.getMilitaryDistrict());
            }
            locations.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Hududga foydalanuvchilar, askarlar yoki guruhlar biriktirilgan, "
                    + "o'chirib bo'lmaydi");
        }
        audit.record("DELETE", "Location", id, description);
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

    private Location find(Long id) {
        return locations.findById(id).orElseThrow(() -> new NotFoundException("Hudud topilmadi"));
    }

    private void requireFreeUnitCode(String code, Long ownUnitId) {
        if (code == null) {
            return;
        }
        militaryUnits.findByCode(code).filter(unit -> !unit.getId().equals(ownUnitId)).ifPresent(unit -> {
            throw new BusinessRuleException("Bu kod bilan harbiy qism allaqachon mavjud");
        });
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Location republic() {
        return locations.findFirstByLevel(LocationLevel.REPUBLIC)
                .orElseThrow(() -> new NotFoundException("Respublika hududi topilmadi"));
    }
}
