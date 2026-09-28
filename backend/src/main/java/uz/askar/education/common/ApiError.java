package uz.askar.education.common;

import java.time.Instant;
import java.util.Map;

/** {@code code} — mijoz uchun mashina o'qiy oladigan sabab (masalan, OTP_REQUIRED); ko'pincha {@code null}. */
public record ApiError(Instant timestamp, int status, String error, String message, Map<String, String> fieldErrors,
                       String code) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message, Map.of(), null);
    }

    public static ApiError of(int status, String error, String message, String code) {
        return new ApiError(Instant.now(), status, error, message, Map.of(), code);
    }
}
