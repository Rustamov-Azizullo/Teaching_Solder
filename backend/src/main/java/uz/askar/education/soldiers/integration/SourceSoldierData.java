package uz.askar.education.soldiers.integration;

import java.time.LocalDate;

/** Manba tizim JShShIR bo'yicha qaytargan maydonlar (API spetsifikatsiyasi olingach moslashtiriladi). */
public record SourceSoldierData(String fullName, LocalDate birthDate, String passport, String regionName,
                                String districtName, String mahalla, String street, String house,
                                String generalEducation) {
}
