package uz.askar.education.common;

/** Biznes qoidasi buzilganda (masalan, takroriy JShShIR) — 409 qaytariladi. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
