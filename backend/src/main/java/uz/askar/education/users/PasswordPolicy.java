package uz.askar.education.users;

import uz.askar.education.common.BusinessRuleException;

/** Murakkab parol siyosati (TT 10-bo'lim, 4-band): kamida 8 belgi, harf, raqam va bosh harf. */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void check(String password) {
        boolean valid = password != null && password.length() >= MIN_LENGTH
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(Character::isUpperCase);
        if (!valid) {
            throw new BusinessRuleException("Parol kamida 8 belgi bo'lib, bosh harf, kichik harf va raqamni o'z ichiga olishi kerak");
        }
    }
}
