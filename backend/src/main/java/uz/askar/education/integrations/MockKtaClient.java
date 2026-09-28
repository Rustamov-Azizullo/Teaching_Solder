package uz.askar.education.integrations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Vaqtinchalik KTA: JShShIR oxirgi raqami 7 bo'lsa "topilmadi", 3 bo'lsa boshqa raqam qaytaradi; qolganlariga mos. */
@Component
public class MockKtaClient implements KtaClient {

    @Override
    public Map<String, String> certificatesByPinfl(Collection<String> pinfls) {
        Map<String, String> result = new HashMap<>();
        for (String pinfl : pinfls) {
            char last = pinfl.charAt(pinfl.length() - 1);
            if (last == '7') {
                continue;
            }
            result.put(pinfl, last == '3' ? "KTA-MISMATCH" : "MOCK-MATCH");
        }
        return result;
    }
}
