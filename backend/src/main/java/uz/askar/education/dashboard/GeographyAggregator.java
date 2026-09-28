package uz.askar.education.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;
import uz.askar.education.assignments.Assignment;
import uz.askar.education.assignments.AssignmentStatus;
import uz.askar.education.dashboard.DashboardDtos.DistrictSoldiers;
import uz.askar.education.dashboard.DashboardDtos.InstitutionContracts;
import uz.askar.education.dashboard.DashboardDtos.RegionInstitutions;
import uz.askar.education.dashboard.DashboardDtos.UnitContract;
import uz.askar.education.dashboard.DashboardDtos.UnitSoldiers;
import uz.askar.education.groups.EducationInstitution;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.soldiers.Soldier;

/** Askarlar va muassasalarning hududiy taqsimotini hisoblovchi sof funksiyalar. */
final class GeographyAggregator {

    private static final String UNKNOWN_REGION = "Hudud ko'rsatilmagan";

    private GeographyAggregator() {
    }

    static List<DistrictSoldiers> districts(List<Soldier> soldiers) {
        Map<MilitaryDistrict, List<Soldier>> byDistrict = soldiers.stream()
                .collect(Collectors.groupingBy(s -> s.getMilitaryUnit().getMilitaryDistrict()));
        return byDistrict.entrySet().stream()
                .map(entry -> new DistrictSoldiers(entry.getKey().getId(), entry.getKey().getName(),
                        entry.getValue().size(), units(entry.getValue())))
                .sorted(Comparator.comparing(DistrictSoldiers::name))
                .toList();
    }

    private static List<UnitSoldiers> units(List<Soldier> districtSoldiers) {
        return districtSoldiers.stream().collect(Collectors.groupingBy(s -> s.getMilitaryUnit()))
                .entrySet().stream()
                .map(entry -> new UnitSoldiers(entry.getKey().getId(), entry.getKey().getName(), entry.getValue().size()))
                .sorted(Comparator.comparing(UnitSoldiers::name))
                .toList();
    }

    /** Rad etilgan takliflar shartnoma hisoblanmaydi; qolgan holatlar status bilan ko'rsatiladi. */
    static List<RegionInstitutions> regions(List<Soldier> soldiers, List<EducationInstitution> institutions,
                                            List<Assignment> assignments) {
        Map<String, Long> soldiersPerRegion = soldiers.stream()
                .collect(Collectors.groupingBy(s -> s.getRegion().getName(), Collectors.counting()));
        Map<Long, List<Assignment>> assignmentsByInstitution = assignments.stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .collect(Collectors.groupingBy(a -> a.getInstitution().getId()));
        Map<String, List<EducationInstitution>> institutionsPerRegion = institutions.stream()
                .collect(Collectors.groupingBy(GeographyAggregator::regionName));

        TreeSet<String> regionNames = new TreeSet<>(soldiersPerRegion.keySet());
        regionNames.addAll(institutionsPerRegion.keySet());

        List<RegionInstitutions> result = new ArrayList<>();
        for (String regionName : regionNames) {
            List<InstitutionContracts> rows = institutionsPerRegion.getOrDefault(regionName, List.of()).stream()
                    .map(institution -> toRow(institution, assignmentsByInstitution.get(institution.getId())))
                    .sorted(Comparator.comparing(InstitutionContracts::name))
                    .toList();
            long contracts = rows.stream().mapToLong(row -> row.units().size()).sum();
            result.add(new RegionInstitutions(regionName, soldiersPerRegion.getOrDefault(regionName, 0L),
                    rows.size(), contracts, rows));
        }
        return result;
    }

    private static InstitutionContracts toRow(EducationInstitution institution, List<Assignment> assignments) {
        Map<Long, UnitContract> byUnit = new LinkedHashMap<>();
        if (assignments != null) {
            assignments.stream().filter(Objects::nonNull).forEach(a -> byUnit.putIfAbsent(a.getMilitaryUnit().getId(),
                    new UnitContract(a.getMilitaryUnit().getId(), a.getMilitaryUnit().getName(),
                            a.getStatus().name(), a.getContractNo(), a.getContractDate())));
        }
        return new InstitutionContracts(institution.getId(), institution.getName(), institution.getType().name(),
                byUnit.values().stream().sorted(Comparator.comparing(UnitContract::unitName)).toList());
    }

    private static String regionName(EducationInstitution institution) {
        return institution.getRegion() == null ? UNKNOWN_REGION : institution.getRegion().getName();
    }
}
