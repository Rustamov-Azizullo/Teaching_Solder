package uz.askar.education.dictionaries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.dictionaries.DictionaryDtos.DictionaryItemDto;
import uz.askar.education.dictionaries.DictionaryDtos.DictionaryItemRequest;
import uz.askar.education.security.AccessScope;

@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryItemRepository items;
    private final UnitDirectionRepository unitDirections;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<DictionaryItemDto> list(DictionaryType type, boolean activeOnly, Long unitId) {
        List<DictionaryItem> found = activeOnly
                ? items.findByTypeAndActiveTrueOrderBySortOrderAscNameAsc(type)
                : items.findByTypeOrderBySortOrderAscNameAsc(type);
        if (type == DictionaryType.PROFESSION_DIRECTION && unitId != null) {
            found = filterByUnitActivation(found, unitId);
        }
        return found.stream().map(DictionaryItemDto::from).toList();
    }

    @Transactional
    public DictionaryItemDto create(DictionaryType type, DictionaryItemRequest request) {
        if (items.existsByTypeAndCode(type, request.code())) {
            throw new BusinessRuleException("Bu kod bilan yozuv allaqachon mavjud");
        }
        DictionaryItem item = new DictionaryItem();
        item.setType(type);
        item.setSortOrder(items.maxSortOrder(type) + 1);
        apply(item, request);
        DictionaryItem saved = items.save(item);
        audit.record("CREATE", "DictionaryItem", saved.getId(), type + ": " + saved.getName());
        return DictionaryItemDto.from(saved);
    }

    @Transactional
    public DictionaryItemDto update(DictionaryType type, Long id, DictionaryItemRequest request) {
        DictionaryItem item = find(type, id);
        String before = item.getName() + " / faol=" + item.isActive();
        apply(item, request);
        audit.record("UPDATE", "DictionaryItem", id, before + " -> " + item.getName() + " / faol=" + item.isActive());
        return DictionaryItemDto.from(item);
    }

    /** Yozuvni o'chiradi. Anketa, guruh va boshqa yozuvlarda ishlatilgan bo'lsa — o'chirilmaydi (faolsizlantirish tavsiya etiladi). */
    @Transactional
    public void delete(DictionaryType type, Long id) {
        DictionaryItem item = find(type, id);
        String name = item.getName();
        try {
            unitDirections.deleteByDirectionId(id);
            items.delete(item);
            items.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Bu yozuv boshqa ma'lumotlarda ishlatilgan, o'chirib bo'lmaydi. "
                    + "Uni faolsizlantiring");
        }
        audit.record("DELETE", "DictionaryItem", id, type + ": " + name);
    }

    @Transactional(readOnly = true)
    public List<Long> unitDirectionIds(Long unitId) {
        return unitDirections.findDirectionIds(unitId);
    }

    @Transactional
    public void replaceUnitDirections(AccessScope scope, Long districtId, Long unitId, List<Long> directionIds) {
        scope.require(districtId, unitId);
        Set<Long> unique = new HashSet<>(directionIds);
        List<DictionaryItem> directions = items.findByIdIn(unique);
        boolean allDirections = directions.size() == unique.size()
                && directions.stream().allMatch(d -> d.getType() == DictionaryType.PROFESSION_DIRECTION);
        if (!allDirections) {
            throw new BusinessRuleException("Yo'nalishlar ro'yxatida noto'g'ri yozuv bor");
        }
        unitDirections.deleteByUnitId(unitId);
        unitDirections.flush();
        unique.forEach(directionId -> unitDirections.save(new UnitDirection(unitId, directionId)));
        audit.record("UPDATE", "UnitDirections", unitId, "Faol yo'nalishlar soni: " + unique.size());
    }

    private List<DictionaryItem> filterByUnitActivation(List<DictionaryItem> all, Long unitId) {
        List<Long> activated = unitDirections.findDirectionIds(unitId);
        // Qism uchun hech narsa tanlanmagan bo'lsa, markaziy ro'yxatdagi barcha faol yo'nalishlar ko'rsatiladi.
        if (activated.isEmpty()) {
            return all;
        }
        return all.stream().filter(item -> activated.contains(item.getId())).toList();
    }

    private DictionaryItem find(DictionaryType type, Long id) {
        return items.findById(id)
                .filter(item -> item.getType() == type)
                .orElseThrow(() -> new NotFoundException("Ma'lumotnoma yozuvi topilmadi"));
    }

    private void apply(DictionaryItem item, DictionaryItemRequest request) {
        item.setCode(request.code());
        item.setName(request.name());
        item.setDescription(request.description());
        item.setHours(request.hours());
        item.setActive(request.active());
    }
}
