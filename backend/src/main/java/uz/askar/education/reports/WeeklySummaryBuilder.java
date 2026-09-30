package uz.askar.education.reports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.export.TableData;
import uz.askar.education.groups.GroupType;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.groups.StudyGroupRepository;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.organization.MilitaryDistrictRepository;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.Subdivision;
import uz.askar.education.organization.SubdivisionRepository;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.security.ScopeLevel;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.soldiers.SoldierRepository;

/**
 * Haftalik hisobot: har bir hudud bo'yicha jami askarlar, kasb kursi va OTM tayyorlovdagilar, kasb guruhlari
 * (kasb va askarlar soni) hamda OTM yo'nalishlari (fanlar) bo'yicha askarlar soni. Kesim vakolat darajasiga mos:
 * SuperAdmin — okruglar, Admin — harbiy qismlar, User — bo'linmalar; {@code TOTAL} ko'rinishida — bitta umumiy hudud.
 * Aniq okrug, harbiy qism yoki bo'linma tanlansa, hisobot faqat shu ob'ekt uchun tuziladi (bo'linma — ichki bo'linmalari bilan).
 */
@Component
@RequiredArgsConstructor
class WeeklySummaryBuilder {

    private static final String ALL_DISTRICTS = "Barcha okruglar";
    private static final String ALL_UNITS = "Barcha harbiy qismlar";
    private static final String ALL_SUBDIVISIONS = "Barcha bo'linmalar";
    private static final String NO_SUBDIVISION = "Bo'linma ko'rsatilmagan";

    /** Tanlangan ob'ekt: askar va guruh filtrlari, hisobotdagi hudud nomi va tavsifi. */
    private record Selection(Predicate<Soldier> soldierFilter, Predicate<StudyGroup> groupFilter,
                             WeeklyUnitRows.Area area, String description, String title, boolean dropEmptyGroups) {
    }

    private final SoldierRepository soldiers;
    private final StudyGroupRepository groups;
    private final SubdivisionRepository subdivisions;
    private final MilitaryUnitRepository units;
    private final MilitaryDistrictRepository districts;
    private final CurrentUser currentUser;
    private final ReportMeta meta;

    TableData build(int year, ReportTarget target) {
        if (!target.hasSingleSelection()) {
            throw new BusinessRuleException("Bir vaqtda faqat bitta okrug, qism yoki bo'linma tanlanadi");
        }
        var scope = currentUser.scope();
        Selection selection = resolveSelection(target);
        Predicate<Soldier> soldierFilter = selection == null ? s -> true : selection.soldierFilter();
        Set<Soldier> scoped = soldiers.findAllInScope(scope.districtFilter(), scope.unitFilter()).stream()
                .filter(soldierFilter).collect(Collectors.toCollection(LinkedHashSet::new));

        Function<Soldier, WeeklyUnitRows.Area> areaOf = areaResolver(scope.level(), target.view(), selection);
        boolean dropEmpty = selection != null && selection.dropEmptyGroups();
        Map<WeeklyUnitRows.Area, List<Soldier>> soldiersByArea = scoped.stream().collect(Collectors.groupingBy(areaOf));
        Map<WeeklyUnitRows.Area, List<WeeklyUnitRows.GroupSlice>> vocational =
                groupSlices(currentGroups(GroupType.VOCATIONAL, year, selection), scoped, areaOf, dropEmpty);
        Map<WeeklyUnitRows.Area, List<WeeklyUnitRows.GroupSlice>> otm =
                groupSlices(currentGroups(GroupType.OTM_PREP, year, selection), scoped, areaOf, dropEmpty);

        Set<WeeklyUnitRows.Area> areas = new LinkedHashSet<>(soldiersByArea.keySet());
        areas.addAll(vocational.keySet());
        areas.addAll(otm.keySet());
        List<List<String>> table = new ArrayList<>();
        areas.stream().sorted(Comparator.comparing(WeeklyUnitRows.Area::district)
                        .thenComparing(WeeklyUnitRows.Area::unit).thenComparing(WeeklyUnitRows.Area::subdivision))
                .forEach(area -> table.addAll(WeeklyUnitRows.forArea(area, soldiersByArea.getOrDefault(area, List.of()),
                        vocational.getOrDefault(area, List.of()), otm.getOrDefault(area, List.of()))));
        return new TableData("Haftalik hisobot: " + titleSuffix(scope.level(), target.view(), selection),
                meta.lines("Yillik sikl: " + year, selection == null ? null : selection.description()),
                WeeklyUnitRows.HEADERS, table);
    }

    private List<StudyGroup> currentGroups(GroupType type, int year, Selection selection) {
        var scope = currentUser.scope();
        Predicate<StudyGroup> groupFilter = selection == null ? g -> true : selection.groupFilter();
        return groups.search(type, scope.districtFilter(), scope.unitFilter()).stream()
                .filter(group -> group.getCycleYear() == year).filter(groupFilter).toList();
    }

    private Selection resolveSelection(ReportTarget target) {
        if (target.subdivisionId() != null) return subdivisionSelection(target.subdivisionId());
        if (target.unitId() != null) return unitSelection(target.unitId());
        if (target.districtId() != null) return districtSelection(target.districtId());
        return null;
    }

    private Selection districtSelection(Long districtId) {
        MilitaryDistrict district = districts.findById(districtId)
                .orElseThrow(() -> new NotFoundException("Okrug topilmadi"));
        currentUser.scope().require(district.getId(), null);
        return new Selection(s -> district.getId().equals(s.getMilitaryUnit().getMilitaryDistrict().getId()),
                g -> district.getId().equals(g.getMilitaryUnit().getMilitaryDistrict().getId()),
                new WeeklyUnitRows.Area(district.getName(), "", ""), "okrug: " + district.getName(),
                "okrug bo'yicha", false);
    }

    private Selection unitSelection(Long unitId) {
        MilitaryUnit unit = units.findById(unitId).orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        return new Selection(s -> unit.getId().equals(s.getMilitaryUnit().getId()),
                g -> unit.getId().equals(g.getMilitaryUnit().getId()),
                new WeeklyUnitRows.Area(unit.getMilitaryDistrict().getName(), unit.getName(), ""),
                "harbiy qism: " + unit.getName(), "harbiy qism bo'yicha", false);
    }

    private Selection subdivisionSelection(Long subdivisionId) {
        Subdivision subdivision = subdivisions.findById(subdivisionId)
                .orElseThrow(() -> new NotFoundException("Bo'linma topilmadi"));
        var unit = subdivision.getMilitaryUnit();
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        Set<Long> ids = subdivisionWithDescendants(subdivision);
        return new Selection(s -> s.getSubdivision() != null && ids.contains(s.getSubdivision().getId()),
                g -> unit.getId().equals(g.getMilitaryUnit().getId()),
                new WeeklyUnitRows.Area(unit.getMilitaryDistrict().getName(), unit.getName(), subdivision.path()),
                "bo'linma: " + subdivision.path(), "bo'linma bo'yicha", true);
    }

    private static String titleSuffix(ScopeLevel level, ReportView view, Selection selection) {
        if (selection != null) return selection.title();
        if (view == ReportView.TOTAL) return "umumiy";
        return switch (level) {
            case REPUBLIC -> "okruglar kesimida";
            case DISTRICT -> "harbiy qismlar kesimida";
            case UNIT -> "bo'linmalar kesimida";
        };
    }

    /** Askarni hisobot hududiga bog'laydi: tanlangan ob'ekt yoki umumiy ko'rinishda — bitta hudud, aks holda daraja bo'yicha. */
    private Function<Soldier, WeeklyUnitRows.Area> areaResolver(ScopeLevel level, ReportView view, Selection selection) {
        if (selection != null) {
            return soldier -> selection.area();
        }
        if (view == ReportView.TOTAL) {
            WeeklyUnitRows.Area total = switch (level) {
                case REPUBLIC -> new WeeklyUnitRows.Area(ALL_DISTRICTS, "", "");
                case DISTRICT -> new WeeklyUnitRows.Area("", ALL_UNITS, "");
                case UNIT -> new WeeklyUnitRows.Area("", "", ALL_SUBDIVISIONS);
            };
            return soldier -> total;
        }
        return soldier -> {
            var unit = soldier.getMilitaryUnit();
            return switch (level) {
                case REPUBLIC -> new WeeklyUnitRows.Area(unit.getMilitaryDistrict().getName(), "", "");
                case DISTRICT -> new WeeklyUnitRows.Area(unit.getMilitaryDistrict().getName(), unit.getName(), "");
                case UNIT -> new WeeklyUnitRows.Area(unit.getMilitaryDistrict().getName(), unit.getName(),
                        soldier.getSubdivision() == null ? NO_SUBDIVISION : soldier.getSubdivision().path());
            };
        };
    }

    /** Guruhlarni hududlarga bo'ladi: har hududga faqat shu hududdagi askarlar kiradi (askarsiz guruhlar ham saqlanadi). */
    private Map<WeeklyUnitRows.Area, List<WeeklyUnitRows.GroupSlice>> groupSlices(
            List<StudyGroup> groupList, Set<Soldier> scoped, Function<Soldier, WeeklyUnitRows.Area> areaOf,
            boolean dropEmpty) {
        Map<WeeklyUnitRows.Area, List<WeeklyUnitRows.GroupSlice>> result = new LinkedHashMap<>();
        for (StudyGroup group : groupList) {
            Map<WeeklyUnitRows.Area, Set<Soldier>> members = group.getSoldiers().stream().filter(scoped::contains)
                    .collect(Collectors.groupingBy(areaOf, LinkedHashMap::new, Collectors.toCollection(LinkedHashSet::new)));
            if (members.isEmpty() && !dropEmpty) {
                members.put(emptyGroupArea(group, areaOf), new LinkedHashSet<>());
            }
            members.forEach((area, slice) -> result.computeIfAbsent(area, key -> new ArrayList<>())
                    .add(new WeeklyUnitRows.GroupSlice(group, slice)));
        }
        return result;
    }

    /** Askarsiz guruh o'z harbiy qismiga qarab hududga joylanadi (areaOf faqat askar qabul qiladi). */
    private WeeklyUnitRows.Area emptyGroupArea(StudyGroup group, Function<Soldier, WeeklyUnitRows.Area> areaOf) {
        Soldier probe = new Soldier();
        probe.setMilitaryUnit(group.getMilitaryUnit());
        return areaOf.apply(probe);
    }

    private Set<Long> subdivisionWithDescendants(Subdivision root) {
        Set<Long> ids = new LinkedHashSet<>(Set.of(root.getId()));
        List<Subdivision> all = subdivisions.findByMilitaryUnitIdOrderByNameAsc(root.getMilitaryUnit().getId());
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Subdivision candidate : all) {
                if (candidate.getParent() != null && ids.contains(candidate.getParent().getId()) && ids.add(candidate.getId())) {
                    grew = true;
                }
            }
        }
        return ids;
    }
}
