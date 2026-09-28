package uz.askar.education.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import uz.askar.education.dashboard.DashboardDtos.CompletionRow;
import uz.askar.education.dashboard.DashboardDtos.CountItem;
import uz.askar.education.dashboard.DashboardDtos.EducationStats;
import uz.askar.education.dashboard.DashboardDtos.SubjectNeed;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.soldiers.CertificateKind;
import uz.askar.education.soldiers.Soldier;
import uz.askar.education.surveys.Questionnaire;

/** Anketa natijalarini umumlashtiradigan sof funksiyalar (TT M12, 3-blok). */
final class SurveyAggregator {

    private static final double PERCENT = 100.0;
    private static final double ROUNDING_FACTOR = 10.0;

    private SurveyAggregator() {
    }

    static List<CompletionRow> completion(List<Soldier> soldiers, List<Questionnaire> finalized) {
        Map<Long, Long> finalizedByUnit = finalized.stream().collect(
                Collectors.groupingBy(q -> q.getSoldier().getMilitaryUnit().getId(), Collectors.counting()));
        Map<Long, List<Soldier>> byUnit = soldiers.stream()
                .collect(Collectors.groupingBy(s -> s.getMilitaryUnit().getId()));
        return byUnit.entrySet().stream().map(entry -> {
            long total = entry.getValue().size();
            long done = finalizedByUnit.getOrDefault(entry.getKey(), 0L);
            String unitName = entry.getValue().get(0).getMilitaryUnit().getName();
            return new CompletionRow(entry.getKey(), unitName, total, done,
                    Math.round(done * PERCENT * ROUNDING_FACTOR / total) / ROUNDING_FACTOR);
        }).sorted(Comparator.comparing(CompletionRow::unitName)).toList();
    }

    static List<CountItem> interests(List<Questionnaire> finalized) {
        return countBy(finalized.stream().map(Questionnaire::getInterestDirection)
                .filter(item -> item != null).toList(), DictionaryItem::getName);
    }

    static List<CountItem> futurePlans(List<Questionnaire> finalized) {
        return countBy(finalized.stream().flatMap(q -> q.getFuturePlans().stream()).toList(),
                DictionaryItem::getName);
    }

    static List<SubjectNeed> subjectNeeds(List<Questionnaire> finalized) {
        Map<String, long[]> counts = new LinkedHashMap<>();
        finalized.forEach(q -> {
            q.getSpecialtySubjects().forEach(subject ->
                    counts.computeIfAbsent(subject.getName(), key -> new long[2])[0]++);
            q.getMandatorySubjects().forEach(subject ->
                    counts.computeIfAbsent(subject.getName(), key -> new long[2])[1]++);
        });
        return counts.entrySet().stream()
                .map(entry -> new SubjectNeed(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .sorted(Comparator.comparingLong((SubjectNeed need) -> need.specialtyCount() + need.mandatoryCount())
                        .reversed())
                .toList();
    }

    static EducationStats education(List<Soldier> soldiers) {
        List<CountItem> levels = new ArrayList<>();
        levels.add(new CountItem("Maktab", countGeneral(soldiers, "SCHOOL")));
        levels.add(new CountItem("Akademik litsey", countGeneral(soldiers, "LYCEUM")));
        levels.add(new CountItem("Professional ta'limga ega",
                soldiers.stream().filter(s -> s.getProfessionalEducation() != null).count()));
        levels.add(new CountItem("Oliy ta'limga ega (tugallanmagan ham)",
                soldiers.stream().filter(s -> s.getHigherEducation() != null).count()));

        List<CountItem> credentials = new ArrayList<>();
        credentials.add(new CountItem("Til sertifikati", countCertificate(soldiers, CertificateKind.LANGUAGE)));
        credentials.add(new CountItem("Kasb sertifikati", countCertificate(soldiers, CertificateKind.PROFESSION)));
        credentials.add(new CountItem("Fan sertifikati", countCertificate(soldiers, CertificateKind.SUBJECT)));
        credentials.add(new CountItem("Sovrindorlar", soldiers.stream().filter(s -> !s.getAwards().isEmpty()).count()));
        return new EducationStats(levels, credentials);
    }

    private static long countGeneral(List<Soldier> soldiers, String level) {
        return soldiers.stream().filter(s -> s.getGeneralEducation().name().equals(level)).count();
    }

    private static long countCertificate(List<Soldier> soldiers, CertificateKind kind) {
        return soldiers.stream().filter(s -> s.getCertificates().stream().anyMatch(c -> c.getKind() == kind)).count();
    }

    private static <T> List<CountItem> countBy(List<T> values, Function<T, String> labelOf) {
        return values.stream().collect(Collectors.groupingBy(labelOf, Collectors.counting())).entrySet().stream()
                .map(entry -> new CountItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(CountItem::count).reversed())
                .toList();
    }
}
