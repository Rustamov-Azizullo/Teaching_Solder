package uz.askar.education.integrations;

import java.util.Optional;
import org.springframework.stereotype.Component;

/** Vaqtinchalik BMBA: oxirgi raqamga qarab turli holatlarni qaytaradi (demo uchun). */
@Component
public class MockBmbaClient implements BmbaClient {

    private static final double BASE_SCORE = 120.0;
    private static final double SCORE_STEP = 6.5;

    @Override
    public Optional<BmbaStatus> findByPinfl(String pinfl) {
        int digit = Character.getNumericValue(pinfl.charAt(pinfl.length() - 1));
        if (digit == 0) {
            return Optional.empty();
        }
        boolean tested = digit >= 3;
        boolean admitted = digit >= 6;
        return Optional.of(new BmbaStatus(true, digit != 1, tested ? BASE_SCORE + digit * SCORE_STEP : null, admitted,
                admitted ? "Mirzo Ulug'bek nomidagi O'zMU" : null, admitted ? "Amaliy matematika" : null));
    }
}
