package uz.askar.education.auth;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** RFC 6238 TOTP (Google Authenticator bilan mos): 30 soniya, 6 raqam, HMAC-SHA1. */
public final class Totp {

    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int STEP_SECONDS = 30;
    private static final int DIGITS_MOD = 1_000_000;
    private static final int SECRET_BYTES = 20;
    private static final int ALLOWED_DRIFT_STEPS = 1;

    private Totp() {
    }

    public static String newSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(bytes);
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;
            while (bits >= 5) {
                out.append(BASE32.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        return out.toString();
    }

    public static boolean verify(String secret, String code, long epochSeconds) {
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long counter = epochSeconds / STEP_SECONDS;
        for (int drift = -ALLOWED_DRIFT_STEPS; drift <= ALLOWED_DRIFT_STEPS; drift++) {
            if (String.format("%06d", generate(secret, counter + drift)).equals(code)) {
                return true;
            }
        }
        return false;
    }

    static int generate(String secret, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decode(secret), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
            return binary % DIGITS_MOD;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("TOTP hisoblab bo'lmadi", ex);
        }
    }

    private static byte[] decode(String secret) {
        ByteBuffer out = ByteBuffer.allocate(secret.length() * 5 / 8);
        int buffer = 0;
        int bits = 0;
        for (char c : secret.toCharArray()) {
            buffer = (buffer << 5) | BASE32.indexOf(c);
            bits += 5;
            if (bits >= 8) {
                out.put((byte) ((buffer >> (bits - 8)) & 0xFF));
                bits -= 8;
            }
        }
        return out.array();
    }
}
