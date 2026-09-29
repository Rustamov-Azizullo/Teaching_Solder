package uz.askar.education.reports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uz.askar.education.groups.StudyGroup;
import uz.askar.education.soldiers.Soldier;

/** Haftalik hisobot qatorlari: bitta harbiy qism uchun umumiy ko'rsatkichlar, kasb guruhlari va OTM yo'nalishlari. */
final class WeeklyUnitRows {

    static final List<String> HEADERS = List.of("Okrug", "Qism", "Bo'lim", "Kasb / yo'nalish", "Guruh", "Askarlar soni");

    private static final String KEY_SEPARATOR = "|";
    private static final String TOTAL = "Jami askarlar";
    private static final String IN_VOCATIONAL = "Kasb kursida";
    private static final String IN_OTM = "OTM tayyorlovda";
    private static final String UNASSIGNED = "Biriktirilmagan";
    private static final String VOCATIONAL_GROUP = "Kasb kursi guruhi";
    private static final String OTM_GROUP = "OTM tayyorlov guruhi";
    private static final String NO_VALUE = "";

    private WeeklyUnitRows() {
    }

    /** "Okrug|Qism" — qismlarni okrug ichida tartiblash va guruhlash uchun. */
    static String unitKey(Soldier soldier) {
        return key(soldier.getMilitaryUnit().getMilitaryDistrict().getName(), soldier.getMilitaryUnit().getName());
    }

    static String unitKey(StudyGroup group) {
        return key(group.getMilitaryUnit().getMilitaryDistrict().getName(), group.getMilitaryUnit().getName());
    }

    static List<List<String>> forUnit(String unitKey, List<Soldier> unitSoldiers, List<StudyGroup> vocationalGroups,
                                      List<StudyGroup> otmGroups) {
        String[] parts = unitKey.split("\\" + KEY_SEPARATOR, 2);
        Set<Soldier> inVocational = soldiersOf(vocationalGroups);
        Set<Soldier> inOtm = soldiersOf(otmGroups);
        long unassigned = unitSoldiers.stream().filter(s -> !inVocational.contains(s) && !inOtm.contains(s)).count();

        List<List<String>> rows = new ArrayList<>();
        rows.add(row(parts, TOTAL, NO_VALUE, NO_VALUE, unitSoldiers.size()));
        rows.add(row(parts, IN_VOCATIONAL, NO_VALUE, NO_VALUE, inVocational.size()));
        rows.add(row(parts, IN_OTM, NO_VALUE, NO_VALUE, inOtm.size()));
        rows.add(row(parts, UNASSIGNED, NO_VALUE, NO_VALUE, unassigned));
        vocationalGroups.stream().sorted(Comparator.comparing(StudyGroup::getName)).forEach(group -> rows.add(
                row(parts, VOCATIONAL_GROUP, group.getProfession() == null ? NO_VALUE : group.getProfession().getName(),
                        group.getName(), group.getSoldiers().size())));
        otmGroups.stream().sorted(Comparator.comparing(StudyGroup::getName)).forEach(group -> rows.add(
                row(parts, OTM_GROUP, subjectsOf(group), group.getName(), group.getSoldiers().size())));
        return rows;
    }

    private static Set<Soldier> soldiersOf(List<StudyGroup> groups) {
        Set<Soldier> result = new LinkedHashSet<>();
        groups.forEach(group -> result.addAll(group.getSoldiers()));
        return result;
    }

    /** OTM tayyorlov yo'nalishi guruh o'qitiladigan fanlar bilan belgilanadi. */
    private static String subjectsOf(StudyGroup group) {
        return group.getSubjects().stream().map(subject -> subject.getName()).sorted().collect(Collectors.joining(", "));
    }

    private static List<String> row(String[] unitParts, String section, String direction, String groupName, long count) {
        return List.of(unitParts[0], unitParts[1], section, direction, groupName, String.valueOf(count));
    }

    private static String key(String districtName, String unitName) {
        return districtName + KEY_SEPARATOR + unitName;
    }
}
