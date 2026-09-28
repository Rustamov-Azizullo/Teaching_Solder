package uz.askar.education.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.security.CurrentUser;

@Service
@RequiredArgsConstructor
public class SubdivisionService {

    public record SubdivisionRequest(@NotBlank @Size(max = 160) String name, Long parentId) {
    }

    public record SubdivisionNode(Long id, String name, Long parentId, long soldierCount,
                                  List<SubdivisionNode> children) {
    }

    private final SubdivisionRepository subdivisions;
    private final MilitaryUnitRepository militaryUnits;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<SubdivisionNode> tree(Long unitId) {
        requireUnit(unitId);
        List<Subdivision> all = subdivisions.findByMilitaryUnitIdOrderByNameAsc(unitId);
        Map<Long, SubdivisionNode> nodes = new HashMap<>();
        all.forEach(s -> nodes.put(s.getId(), new SubdivisionNode(s.getId(), s.getName(),
                s.getParent() == null ? null : s.getParent().getId(), subdivisions.countSoldiers(s.getId()),
                new ArrayList<>())));
        List<SubdivisionNode> roots = new ArrayList<>();
        nodes.values().stream().sorted((a, b) -> a.name().compareTo(b.name())).forEach(node -> {
            if (node.parentId() == null) {
                roots.add(node);
            } else {
                nodes.get(node.parentId()).children().add(node);
            }
        });
        return roots;
    }

    @Transactional
    public SubdivisionNode create(Long unitId, SubdivisionRequest request) {
        MilitaryUnit unit = requireUnit(unitId);
        Subdivision subdivision = new Subdivision();
        subdivision.setMilitaryUnit(unit);
        subdivision.setName(request.name().trim());
        subdivision.setParent(parentOf(unit, request.parentId()));
        Subdivision saved = subdivisions.save(subdivision);
        audit.record("CREATE", "Subdivision", saved.getId(), saved.path());
        return new SubdivisionNode(saved.getId(), saved.getName(), request.parentId(), 0, List.of());
    }

    @Transactional
    public SubdivisionNode update(Long id, SubdivisionRequest request) {
        Subdivision subdivision = find(id);
        Subdivision parent = parentOf(subdivision.getMilitaryUnit(), request.parentId());
        for (Subdivision cursor = parent; cursor != null; cursor = cursor.getParent()) {
            if (cursor.getId().equals(id)) {
                throw new BusinessRuleException("Bo'linmani o'zining ichiga ko'chirib bo'lmaydi");
            }
        }
        subdivision.setName(request.name().trim());
        subdivision.setParent(parent);
        audit.record("UPDATE", "Subdivision", id, subdivision.path());
        return new SubdivisionNode(id, subdivision.getName(), request.parentId(),
                subdivisions.countSoldiers(id), List.of());
    }

    @Transactional
    public void delete(Long id) {
        Subdivision subdivision = find(id);
        if (subdivisions.existsByParentId(id) || subdivisions.countSoldiers(id) > 0) {
            throw new BusinessRuleException("Ichida bo'linma yoki askar bor bo'linmani o'chirib bo'lmaydi");
        }
        subdivisions.delete(subdivision);
        audit.record("DELETE", "Subdivision", id, subdivision.path());
    }

    /** Bo'linma va uning barcha ichki bo'linmalari id lari (askarlarni bo'linma bo'yicha filtrlash uchun). */
    @Transactional(readOnly = true)
    public Set<Long> withDescendants(Long id) {
        Subdivision root = find(id);
        List<Subdivision> all = subdivisions.findByMilitaryUnitIdOrderByNameAsc(root.getMilitaryUnit().getId());
        Set<Long> result = new HashSet<>(Set.of(id));
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Subdivision s : all) {
                if (s.getParent() != null && result.contains(s.getParent().getId()) && result.add(s.getId())) {
                    grew = true;
                }
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Subdivision findInScope(Long id) {
        return find(id);
    }

    /** Ko'chirish uchun: nishon bo'linma boshqa qismda bo'lishi mumkin, shuning uchun vakolat tekshirilmaydi. */
    @Transactional(readOnly = true)
    public Subdivision findInScopeAnyUnit(Long id) {
        return subdivisions.findById(id).orElseThrow(() -> new NotFoundException("Bo'linma topilmadi"));
    }

    private Subdivision find(Long id) {
        Subdivision subdivision = subdivisions.findById(id)
                .orElseThrow(() -> new NotFoundException("Bo'linma topilmadi"));
        requireUnit(subdivision.getMilitaryUnit().getId());
        return subdivision;
    }

    private Subdivision parentOf(MilitaryUnit unit, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Subdivision parent = find(parentId);
        if (!parent.getMilitaryUnit().getId().equals(unit.getId())) {
            throw new BusinessRuleException("Ota bo'linma boshqa harbiy qismga tegishli");
        }
        return parent;
    }

    private MilitaryUnit requireUnit(Long unitId) {
        MilitaryUnit unit = militaryUnits.findById(unitId)
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        return unit;
    }
}
