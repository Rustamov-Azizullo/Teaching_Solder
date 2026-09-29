package uz.askar.education.dashboard;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uz.askar.education.dashboard.DashboardDtos.DistrictSoldiers;
import uz.askar.education.dashboard.DashboardDtos.UnitSoldiers;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.soldiers.Soldier;

/** Askarlarning okrug va harbiy qism kesimidagi taqsimotini hisoblovchi sof funksiyalar. */
final class GeographyAggregator {

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
}
