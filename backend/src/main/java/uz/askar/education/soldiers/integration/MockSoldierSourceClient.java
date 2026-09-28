package uz.askar.education.soldiers.integration;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Vaqtinchalik (mock) manba tizim: API spetsifikatsiyasi berilmaguncha ishlatiladi.
 * JShShIR "0" bilan boshlansa — topilmadi, "9" bilan boshlansa — manba tizim javob bermaydi.
 */
@Component
public class MockSoldierSourceClient implements SoldierSourceClient {

    private static final List<String> NAMES = List.of(
            "Karimov Jasur Bahodir o'g'li", "Aliyev Sardor Anvar o'g'li", "Rahimov Otabek Ilhom o'g'li",
            "Yusupov Bekzod Farrux o'g'li", "Tursunov Dilshod Rustam o'g'li", "Xolmatov Sherzod Ulug'bek o'g'li");
    private static final List<String> PASSPORT_SERIES = List.of("AA", "AB", "AC", "KA");
    private static final int PINFL_SEED_LENGTH = 9;
    private static final int MIN_BIRTH_YEAR = 2003;
    private static final int BIRTH_YEAR_SPAN = 4;
    private static final int MONTHS_IN_YEAR = 12;
    private static final int SAFE_DAYS_IN_MONTH = 28;
    private static final int PASSPORT_NUMBER_DIGITS = 7;
    private static final String GENERAL_EDUCATION_SCHOOL = "SCHOOL";

    @Override
    public Optional<SourceSoldierData> findByPinfl(String pinfl) {
        if (pinfl.startsWith("9")) {
            throw new SourceSystemUnavailableException("Manba tizim javob bermadi (mock)");
        }
        if (pinfl.startsWith("0")) {
            return Optional.empty();
        }
        int seed = Integer.parseInt(pinfl.substring(pinfl.length() - PINFL_SEED_LENGTH));
        LocalDate birthDate = LocalDate.of(MIN_BIRTH_YEAR + seed % BIRTH_YEAR_SPAN,
                1 + seed % MONTHS_IN_YEAR, 1 + seed % SAFE_DAYS_IN_MONTH);
        String passport = PASSPORT_SERIES.get(seed % PASSPORT_SERIES.size())
                + String.format("%0" + PASSPORT_NUMBER_DIGITS + "d", seed % 10_000_000);
        return Optional.of(new SourceSoldierData(NAMES.get(seed % NAMES.size()), birthDate, passport,
                "Toshkent shahri", "Chilonzor tumani", "Bunyodkor MFY", "Bunyodkor ko'chasi",
                String.valueOf(1 + seed % 90), GENERAL_EDUCATION_SCHOOL));
    }
}
