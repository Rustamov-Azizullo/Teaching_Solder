package uz.askar.education.reports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.soldiers.Soldier;

/** Haftalik hisobot qatorlari: bitta hudud (okrug, qism yoki bo'linma) uchun umumiy ko'rsatkichlar, kasb guruhlari va OTM yo'nalishlari. */
final class WeeklyUnitRows {

    static final List<String> HEADERS = List.of("Okrug", "Qism", "Bo'linma", "Bo'lim", "Kasb / yo'nalish", "Guruh",
            "Askarlar soni");

    private static final String TOTAL = "Jami askarlar";
    private static final String IN_VOCATIONAL = "Kasb kursida";
    private static final String IN_OTM = "OTM tayyorlovda";
    private static final String UNASSIGNED = "Biriktirilmagan";
    private static final String VOCATIONAL_GROUP = "Kasb kursi guruhi";
    private static final String OTM_GROUP = "OTM tayyorlov guruhi";
    private static final String NO_VALUE = "";

    /** Hisobot qatorlari guruhlanadigan hudud; ishlatilmaydigan darajalar bo'sh qoladi. */
    record Area(String district, String unit, String subdivision) {
    }

    /** Guruhning ma'lum bir hududga tegishli askarlari. */
    record GroupSlice(StudyGroup group, Set<Soldier> members) {
    }

    private WeeklyUnitRows() {
    }

    static List<List<String>> forArea(Area area, List<Soldier> areaSoldiers, List<GroupSlice> vocationalGroups,
                                      List<GroupSlice> otmGroups) {
        Set<Soldier> inVocational = soldiersOf(vocationalGroups);
        Set<Soldier> inOtm = soldiersOf(otmGroups);
        long unassigned = areaSoldiers.stream().filter(s -> !inVocational.contains(s) && !inOtm.contains(s)).count();

        List<List<String>> rows = new ArrayList<>();
        rows.add(row(area, TOTAL, NO_VALUE, NO_VALUE, areaSoldiers.size()));
        rows.add(row(area, IN_VOCATIONAL, NO_VALUE, NO_VALUE, inVocational.size()));
        rows.add(row(area, IN_OTM, NO_VALUE, NO_VALUE, inOtm.size()));
        rows.add(row(area, UNASSIGNED, NO_VALUE, NO_VALUE, unassigned));
        byGroupName(vocationalGroups).forEach(slice -> rows.add(row(area, VOCATIONAL_GROUP,
                slice.group().getProfession() == null ? NO_VALUE : slice.group().getProfession().getName(),
                slice.group().getName(), slice.members().size())));
        byGroupName(otmGroups).forEach(slice -> rows.add(row(area, OTM_GROUP, subjectsOf(slice.group()),
                slice.group().getName(), slice.members().size())));
        return rows;
    }

    private static List<GroupSlice> byGroupName(List<GroupSlice> slices) {
        return slices.stream().sorted(Comparator.comparing(slice -> slice.group().getName())).toList();
    }

    private static Set<Soldier> soldiersOf(List<GroupSlice> slices) {
        Set<Soldier> result = new LinkedHashSet<>();
        slices.forEach(slice -> result.addAll(slice.members()));
        return result;
    }

    /** OTM tayyorlov yo'nalishi guruh o'qitiladigan fanlar bilan belgilanadi. */
    private static String subjectsOf(StudyGroup group) {
        return group.getSubjects().stream().map(subject -> subject.getName()).sorted().collect(Collectors.joining(", "));
    }

    private static List<String> row(Area area, String section, String direction, String groupName, long count) {
        return List.of(area.district(), area.unit(), area.subdivision(), section, direction, groupName,
                String.valueOf(count));
    }
}
