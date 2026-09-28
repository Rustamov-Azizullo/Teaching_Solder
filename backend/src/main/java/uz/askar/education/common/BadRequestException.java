package uz.askar.education.common;

/** So'rov mazmuni noto'g'ri (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
