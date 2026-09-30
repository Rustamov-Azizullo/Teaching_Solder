package uz.askar.education.dashboard;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.dashboard.DashboardDtos.CatalogCounts;
import uz.askar.education.dashboard.DashboardDtos.CatalogEntry;
import uz.askar.education.dashboard.DashboardDtos.CatalogUnit;
import uz.askar.education.dashboard.DashboardDtos.ScopedCatalog;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.dictionaries.UnitDirectionRepository;
import uz.askar.education.groups.GroupType;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.groups.StudyGroupRepository;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.security.CurrentUser;

/**
 * Vakolat doirasidagi harbiy qismlarda mavjud kasb yo'nalishlari, kasblar va fanlar. Yo'nalishlar — qism uchun faollashtirilgan
 * (hech narsa tanlanmagan bo'lsa, markaziy ro'yxatdagi barcha faol yo'nalishlar); kasblar va fanlar — joriy sikldagi
 * kasb kursi va OTM tayyorlov guruhlarida o'qitilayotganlar.
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MilitaryUnitRepository units;
    private final StudyGroupRepository groups;
    private final DictionaryItemRepository items;
    private final UnitDirectionRepository unitDirections;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public ScopedCatalog catalog() {
        var scope = currentUser.scope();
        int year = LocalDate.now().getYear();
        List<DictionaryItem> activeDirections = items.findByTypeAndActiveTrueOrderBySortOrderAscNameAsc(
                DictionaryType.PROFESSION_DIRECTION);
        Map<Long, List<StudyGroup>> vocationalByUnit = groupsByUnit(GroupType.VOCATIONAL, year);
        Map<Long, List<StudyGroup>> otmByUnit = groupsByUnit(GroupType.OTM_PREP, year);

        List<CatalogUnit> unitRows = units.findInScope(scope.districtFilter(), scope.unitFilter()).stream()
                .map(unit -> toUnit(unit, activeDirections, vocationalByUnit.getOrDefault(unit.getId(), List.of()),
                        otmByUnit.getOrDefault(unit.getId(), List.of())))
                .toList();
        return new ScopedCatalog(countsOf(unitRows), unitRows);
    }

    /** Ma'lumotnomadagi faol yozuvlar soni (respublika darajasi uchun — vakolat doirasi cheklanmagan). */
    @Transactional(readOnly = true)
    public CatalogCounts globalCounts() {
        return new CatalogCounts(items.countByTypeAndActiveTrue(DictionaryType.PROFESSION_DIRECTION),
                items.countByTypeAndActiveTrue(DictionaryType.SUBJECT), items.countByTypeAndActiveTrue(DictionaryType.PROFESSION));
    }

    private Map<Long, List<StudyGroup>> groupsByUnit(GroupType type, int year) {
        var scope = currentUser.scope();
        return groups.search(type, scope.districtFilter(), scope.unitFilter()).stream()
                .filter(group -> group.getCycleYear() == year)
                .collect(Collectors.groupingBy(group -> group.getMilitaryUnit().getId()));
    }

    private CatalogUnit toUnit(MilitaryUnit unit, List<DictionaryItem> activeDirections, List<StudyGroup> vocational,
                               List<StudyGroup> otm) {
        Set<Long> activated = Set.copyOf(unitDirections.findDirectionIds(unit.getId()));
        List<String> directionNames = activeDirections.stream()
                .filter(direction -> activated.isEmpty() || activated.contains(direction.getId()))
                .map(DictionaryItem::getName).toList();
        return new CatalogUnit(unit.getId(), unit.getName(), directionNames,
                entries(vocational.stream().filter(group -> group.getProfession() != null).collect(
                        Collectors.groupingBy(group -> group.getProfession().getName(), LinkedHashMap::new, Collectors.toList()))),
                entries(otm.stream().flatMap(group -> group.getSubjects().stream().map(subject -> Map.entry(subject.getName(), group)))
                        .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())))));
    }

    /** Nom bo'yicha guruhlar: nechta guruhda va nechta askar (bir askar bir marta) o'qitilayotgani. */
    private List<CatalogEntry> entries(Map<String, List<StudyGroup>> groupsByName) {
        return groupsByName.entrySet().stream()
                .map(entry -> new CatalogEntry(entry.getKey(), entry.getValue().size(),
                        entry.getValue().stream().flatMap(group -> group.getSoldiers().stream()).distinct().count()))
                .sorted(Comparator.comparingLong(CatalogEntry::soldiers).reversed().thenComparing(CatalogEntry::name))
                .toList();
    }

    private CatalogCounts countsOf(List<CatalogUnit> unitRows) {
        return new CatalogCounts(
                distinct(unitRows.stream().flatMap(unit -> unit.directions().stream())),
                distinct(unitRows.stream().flatMap(unit -> unit.subjects().stream().map(CatalogEntry::name))),
                distinct(unitRows.stream().flatMap(unit -> unit.professions().stream().map(CatalogEntry::name))));
    }

    private long distinct(java.util.stream.Stream<String> names) {
        return new TreeSet<>(names.toList()).size();
    }
}
