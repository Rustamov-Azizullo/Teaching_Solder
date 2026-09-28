package uz.askar.education.soldiers.integration;

import java.util.Optional;

/**
 * Manba tizim (chaqiriluvchilar bazasi) bilan aloqa abstraksiyasi (DIP).
 * Haqiqiy API spetsifikatsiyasi taqdim etilgach, faqat shu interfeysning yangi implementatsiyasi yoziladi.
 */
public interface SoldierSourceClient {

    /**
     * @return topilsa ma'lumot, topilmasa bo'sh
     * @throws SourceSystemUnavailableException manba tizim javob bermasa
     */
    Optional<SourceSoldierData> findByPinfl(String pinfl);
}
