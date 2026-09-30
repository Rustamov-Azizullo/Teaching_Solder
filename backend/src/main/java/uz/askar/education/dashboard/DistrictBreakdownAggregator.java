package uz.askar.education.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import uz.askar.education.dashboard.DashboardDtos.DistrictRow;
import uz.askar.education.dashboard.DashboardDtos.ProgramRow;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.Subdivision;
import uz.askar.education.security.ScopeLevel;
import uz.askar.education.soldiers.Soldier;

/**
 * Vakolat darajasiga mos kesim: respublika darajasida okruglar, okrug darajasida harbiy qismlar, qism darajasida
 * bo'linmalar. Har bir qator: askarlar, kasb va OTM kurslari, kasblar va muassasalar soni.
 */
final class DistrictBreakdownAggregator {

    private static final String NO_INSTITUTION = "Muassasa ko'rsatilmagan";
    private static final String NO_SUBDIVISION = "Bo'linma ko'rsatilmagan";

    private DistrictBreakdownAggregator() {
    }

    /** Kesim birligi (okrug, qism yoki bo'linma); {@code id} bo'linma ko'rsatilmagan askarlar uchun {@code null}. */
    private record Bucket(Long id, String name) {
    }

    /** Guruhning ma'lum bir kesim birligiga tegishli askarlari. */
    private record GroupSlice(StudyGroup group, Set<Soldier> soldiers) {
    }

    static List<DistrictRow> districts(List<Soldier> soldiers, List<StudyGroup> vocationalGroups,
                                       List<StudyGroup> otmGroups, ScopeLevel level) {
        Function<Soldier, Bucket> soldierBucket = soldierBucket(level);
        Map<Bucket, List<Soldier>> soldiersByBucket = soldiers.stream().collect(Collectors.groupingBy(soldierBucket));
        Map<Bucket, List<GroupSlice>> vocationalByBucket = slices(vocationalGroups, level, soldierBucket);
        Map<Bucket, List<GroupSlice>> otmByBucket = slices(otmGroups, level, soldierBucket);

        Set<Bucket> allBuckets = new LinkedHashSet<>(soldiersByBucket.keySet());
        allBuckets.addAll(vocationalByBucket.keySet());
        allBuckets.addAll(otmByBucket.keySet());
        return allBuckets.stream()
                .map(bucket -> toRow(bucket, soldiersByBucket.getOrDefault(bucket, List.of()),
                        vocationalByBucket.getOrDefault(bucket, List.of()),
                        otmByBucket.getOrDefault(bucket, List.of())))
                .sorted(Comparator.comparing(DistrictRow::name))
                .toList();
    }

    private static Function<Soldier, Bucket> soldierBucket(ScopeLevel level) {
        return switch (level) {
            case REPUBLIC -> soldier -> districtBucket(soldier.getMilitaryUnit());
            case DISTRICT -> soldier -> unitBucket(soldier.getMilitaryUnit());
            case UNIT -> soldier -> subdivisionBucket(soldier.getSubdivision());
        };
    }

    private static Bucket districtBucket(MilitaryUnit unit) {
        return new Bucket(unit.getMilitaryDistrict().getId(), unit.getMilitaryDistrict().getName());
    }

    private static Bucket unitBucket(MilitaryUnit unit) {
        return new Bucket(unit.getId(), unit.getName());
    }

    private static Bucket subdivisionBucket(Subdivision subdivision) {
        return subdivision == null ? new Bucket(null, NO_SUBDIVISION) : new Bucket(subdivision.getId(), subdivision.getName());
    }

    /** Okrug/qism darajasida guruh butunligicha o'z qismiga tegishli; bo'linma darajasida askarlari bo'yicha bo'linadi. */
    private static Map<Bucket, List<GroupSlice>> slices(List<StudyGroup> groups, ScopeLevel level,
                                                        Function<Soldier, Bucket> soldierBucket) {
        Map<Bucket, List<GroupSlice>> result = new LinkedHashMap<>();
        for (StudyGroup group : groups) {
            if (level == ScopeLevel.UNIT) {
                group.getSoldiers().stream().collect(Collectors.groupingBy(soldierBucket, Collectors.toSet()))
                        .forEach((bucket, members) ->
                                result.computeIfAbsent(bucket, key -> new ArrayList<>()).add(new GroupSlice(group, members)));
                continue;
            }
            Bucket bucket = level == ScopeLevel.REPUBLIC
                    ? districtBucket(group.getMilitaryUnit()) : unitBucket(group.getMilitaryUnit());
            result.computeIfAbsent(bucket, key -> new ArrayList<>())
                    .add(new GroupSlice(group, new LinkedHashSet<>(group.getSoldiers())));
        }
        return result;
    }

    private static DistrictRow toRow(Bucket bucket, List<Soldier> soldiers, List<GroupSlice> vocational,
                                     List<GroupSlice> otm) {
        long professions = vocational.stream().map(GroupSlice::group).filter(g -> g.getProfession() != null)
                .map(g -> g.getProfession().getId()).distinct().count();
        long institutions = vocational.stream().map(GroupSlice::group).filter(g -> g.getInstitution() != null)
                .map(g -> g.getInstitution().getId()).distinct().count();
        return new DistrictRow(bucket.id(), bucket.name(), soldiers.size(), distinctSoldiers(vocational),
                distinctSoldiers(otm), professions, institutions, programs(vocational));
    }

    private static long distinctSoldiers(List<GroupSlice> slices) {
        return slices.stream().flatMap(slice -> slice.soldiers().stream()).distinct().count();
    }

    /** Kasb + muassasa juftliklari bo'yicha o'qiyotgan askarlar (bir askar bir marta sanaladi). */
    private static List<ProgramRow> programs(List<GroupSlice> vocational) {
        Map<List<String>, Set<Soldier>> byProgram = new LinkedHashMap<>();
        vocational.stream().filter(slice -> slice.group().getProfession() != null).forEach(slice ->
                byProgram.computeIfAbsent(List.of(slice.group().getProfession().getName(), institutionName(slice.group())),
                        key -> new LinkedHashSet<>()).addAll(slice.soldiers()));
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
