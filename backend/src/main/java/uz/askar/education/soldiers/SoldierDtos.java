package uz.askar.education.soldiers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class SoldierDtos {

    public static final String PINFL_REGEX = "\\d{14}";
    public static final String PASSPORT_REGEX = "[A-Z]{2}\\d{7}";
    public static final String PHONE_REGEX = "\\+998\\d{9}";

    private SoldierDtos() {
    }

    public record CertificateInput(@NotNull CertificateKind kind, Long dictionaryItemId,
                                   @Size(max = 255) String title, @Size(max = 60) String level) {
    }

    public record AwardInput(@NotNull Long kindId, @NotNull AwardPlace place) {
    }

    public record SoldierRequest(
            @NotBlank @Pattern(regexp = PINFL_REGEX, message = "JShShIR 14 ta raqamdan iborat bo'lishi kerak")
            String pinfl,
            @NotBlank @Size(max = 200) String fullName,
            @NotNull @Past LocalDate birthDate,
            @NotBlank @Pattern(regexp = PASSPORT_REGEX, message = "Pasport formati: AA1234567") String passport,
            @NotBlank @Pattern(regexp = PHONE_REGEX, message = "Telefon formati: +998XXXXXXXXX") String phone,
            @NotNull Long phoneKinshipId,
            @NotNull Long regionId,
            @NotNull Long districtId,
            @NotBlank String mahalla,
            @NotBlank String street,
            @NotBlank String house,
            String apartment,
            @NotNull Long militaryUnitId,
            LocalDate conscriptionDate,
            LocalDate serviceEndDate,
            @NotNull GeneralEducation generalEducation,
            ProfessionalEducation professionalEducation,
            HigherEducation higherEducation,
            @Size(max = 255) String priorOccupation,
            boolean noPriorOccupation,
            Boolean trainable,
            @Valid List<CertificateInput> certificates,
            @Valid List<AwardInput> awards,
            Map<String, FieldSource> fieldSources,
            Long subdivisionId) {
    }

    public record CertificateDto(Long id, CertificateKind kind, Long dictionaryItemId, String dictionaryItemName,
                                 String title, String level) {
    }

    public record AwardDto(Long id, Long kindId, String kindName, AwardPlace place) {
    }

    public record FieldSourceDto(FieldSource source, String filledBy, LocalDateTime filledAt) {
    }

    public record SoldierDto(
            Long id, String pinfl, String fullName, LocalDate birthDate, String passport, String phone,
            Long phoneKinshipId, String phoneKinshipName, Long regionId, String regionName, Long districtId,
            String districtName, String mahalla, String street, String house, String apartment,
            Long militaryUnitId, String militaryUnitName, LocalDate conscriptionDate, LocalDate serviceEndDate,
            GeneralEducation generalEducation, ProfessionalEducation professionalEducation,
            HigherEducation higherEducation, String priorOccupation, boolean noPriorOccupation, Boolean trainable,
            int cycleYear, List<CertificateDto> certificates, List<AwardDto> awards,
            Map<String, FieldSourceDto> fieldSources, Long subdivisionId, String subdivisionPath) {
    }

    public record SoldierSummary(Long id, String pinfl, String fullName, LocalDate birthDate,
                                 String militaryUnitName, String regionName, String districtName,
                                 String subdivisionPath) {
    }

    /** JShShIR bo'yicha manba tizimdan olingan, formaga oldindan to'ldiriladigan maydonlar. */
    public record SourceLookupResponse(boolean found, Long existingSoldierId, String fullName,
                                       LocalDate birthDate, String passport, Long regionId, Long districtId,
                                       String mahalla, String street, String house,
                                       GeneralEducation generalEducation) {

        public static SourceLookupResponse notFound(Long existingSoldierId) {
            return new SourceLookupResponse(false, existingSoldierId, null, null, null, null, null, null, null,
                    null, null);
        }
    }
}
