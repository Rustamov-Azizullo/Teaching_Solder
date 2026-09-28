package uz.askar.education.auth;

public class InvalidCredentialsException extends RuntimeException {

    public static final String OTP_REQUIRED = "OTP_REQUIRED";
    public static final String OTP_INVALID = "OTP_INVALID";

    private final String code;

    public InvalidCredentialsException(String message) {
        this(message, null);
    }

    public InvalidCredentialsException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
