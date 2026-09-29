package uz.askar.education.dashboard;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uz.askar.education.dashboard.DashboardDtos.DistrictRow;
import uz.askar.education.dashboard.DashboardDtos.ProgramRow;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.organization.MilitaryDistrict;
import uz.askar.education.soldiers.Soldier;

/** Okruglar kesimidagi ko'rsatkichlar: askarlar, kasb va OTM kurslari, kasblar va muassasalar soni. */
final class DistrictBreakdownAggregator {

    private static final String NO_INSTITUTION = "Muassasa ko'rsatilmagan";

    private DistrictBreakdownAggregator() {
    }

    static List<DistrictRow> districts(List<Soldier> soldiers, List<StudyGroup> vocationalGroups,
                                       List<StudyGroup> otmGroups) {
        Map<MilitaryDistrict, List<Soldier>> soldiersByDistrict = soldiers.stream()
                .collect(Collectors.groupingBy(s -> s.getMilitaryUnit().getMilitaryDistrict()));
        Map<MilitaryDistrict, List<StudyGroup>> vocationalByDistrict = byDistrict(vocationalGroups);
        Map<MilitaryDistrict, List<StudyGroup>> otmByDistrict = byDistrict(otmGroups);

        Set<MilitaryDistrict> allDistricts = new LinkedHashSet<>(soldiersByDistrict.keySet());
        allDistricts.addAll(vocationalByDistrict.keySet());
        allDistricts.addAll(otmByDistrict.keySet());
        return allDistricts.stream()
                .map(district -> toRow(district, soldiersByDistrict.getOrDefault(district, List.of()),
                        vocationalByDistrict.getOrDefault(district, List.of()),
                        otmByDistrict.getOrDefault(district, List.of())))
                .sorted(Comparator.comparing(DistrictRow::name))
                .toList();
    }

    private static Map<MilitaryDistrict, List<StudyGroup>> byDistrict(List<StudyGroup> groups) {
        return groups.stream().collect(Collectors.groupingBy(g -> g.getMilitaryUnit().getMilitaryDistrict()));
    }

    private static DistrictRow toRow(MilitaryDistrict district, List<Soldier> soldiers,
                                     List<StudyGroup> vocationalGroups, List<StudyGroup> otmGroups) {
        long professions = vocationalGroups.stream().filter(g -> g.getProfession() != null)
                .map(g -> g.getProfession().getId()).distinct().count();
        long institutions = vocationalGroups.stream().filter(g -> g.getInstitution() != null)
                .map(g -> g.getInstitution().getId()).distinct().count();
        return new DistrictRow(district.getId(), district.getName(), soldiers.size(),
                OverviewAggregator.soldiersOf(vocationalGroups).size(), OverviewAggregator.soldiersOf(otmGroups).size(),
                professions, institutions, programs(vocationalGroups));
    }

    /** Kasb + muassasa juftliklari bo'yicha o'qiyotgan askarlar (bir askar bir marta sanaladi). */
    private static List<ProgramRow> programs(List<StudyGroup> vocationalGroups) {
        Map<List<String>, Set<Soldier>> byProgram = new LinkedHashMap<>();
        vocationalGroups.stream().filter(g -> g.getProfession() != null).forEach(group ->
                byProgram.computeIfAbsent(List.of(group.getProfession().getName(), institutionName(group)),
                        key -> new LinkedHashSet<>()).addAll(group.getSoldiers()));
        return byProgram.entrySet().stream()
                .map(entry -> new ProgramRow(entry.getKey().get(0), entry.getKey().get(1), entry.getValue().size()))
                .sorted(Comparator.comparingLong(ProgramRow::soldiers).reversed()
                        .thenComparing(ProgramRow::profession).thenComparing(ProgramRow::institution))
                .toList();
    }

    private static String institutionName(StudyGroup group) {
        return group.getInstitution() == null ? NO_INSTITUTION : group.getInstitution().getName();
    }
}
