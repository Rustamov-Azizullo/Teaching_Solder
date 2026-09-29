package uz.askar.education.dashboard;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uz.askar.education.dashboard.DashboardDtos.ProfessionRow;
import uz.askar.education.dashboard.DashboardDtos.RegionRow;
import uz.askar.education.organization.Region;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.soldiers.HigherEducation;
import uz.askar.education.soldiers.Soldier;

/** Yagona dashboard uchun sof agregatlar: kurslardagi askarlar, kasblar kesimi va viloyatlar bo'yicha askarlar. */
final class OverviewAggregator {

    private OverviewAggregator() {
    }

    /** Guruhlardagi noyob askarlar (bir askar bir nechta guruhda bo'lsa ham bir marta sanaladi). */
    static Set<Soldier> soldiersOf(List<StudyGroup> groups) {
        Set<Soldier> result = new LinkedHashSet<>();
        groups.forEach(group -> result.addAll(group.getSoldiers()));
        return result;
    }

    static long countCertified(List<Soldier> soldiers) {
        return soldiers.stream().filter(s -> !s.getCertificates().isEmpty()).count();
    }

    static long countHigher(List<Soldier> soldiers, boolean completed) {
        return soldiers.stream().filter(s -> s.getHigherEducation() != null
                && (s.getHigherEducation() != HigherEducation.INCOMPLETE_HIGHER) == completed).count();
    }

    /** Kasb kurslari guruhlaridagi askarlar kasb bo'yicha; har kasb uchun okrug va harbiy qism taqsimoti bilan. */
    static List<ProfessionRow> professions(List<StudyGroup> vocationalGroups) {
        Map<String, Set<Soldier>> byProfession = new LinkedHashMap<>();
        vocationalGroups.stream().filter(group -> group.getProfession() != null).forEach(group ->
                byProfession.computeIfAbsent(group.getProfession().getName(), key -> new LinkedHashSet<>())
                        .addAll(group.getSoldiers()));
        return byProfession.entrySet().stream()
                .map(entry -> new ProfessionRow(entry.getKey(), entry.getValue().size(),
                        GeographyAggregator.districts(List.copyOf(entry.getValue()))))
                .sorted(Comparator.comparingLong(ProfessionRow::soldiers).reversed()
                        .thenComparing(ProfessionRow::profession))
                .toList();
    }

    /** Askarlar soni viloyatlar bo'yicha; askari yo'q hududlar ham nol bilan ko'rsatiladi. */
    static List<RegionRow> regions(List<Region> allRegions, List<Soldier> soldiers) {
        Map<Long, Long> countByRegionId = soldiers.stream().filter(s -> s.getRegion() != null)
                .collect(Collectors.groupingBy(s -> s.getRegion().getId(), Collectors.counting()));
        return allRegions.stream()
                .map(region -> new RegionRow(region.getName(), countByRegionId.getOrDefault(region.getId(), 0L)))
                .sorted(Comparator.comparingLong(RegionRow::soldiers).reversed().thenComparing(RegionRow::name))
                .toList();
    }
}
