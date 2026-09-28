package uz.askar.education.soldiers;

import java.util.LinkedHashMap;
import java.util.Map;
import uz.askar.education.soldiers.SoldierDtos.AwardDto;
import uz.askar.education.soldiers.SoldierDtos.CertificateDto;
import uz.askar.education.soldiers.SoldierDtos.FieldSourceDto;
import uz.askar.education.soldiers.SoldierDtos.SoldierDto;
import uz.askar.education.soldiers.SoldierDtos.SoldierSummary;

final class SoldierMapper {

    private SoldierMapper() {
    }

    static SoldierSummary toSummary(Soldier soldier) {
        return new SoldierSummary(soldier.getId(), soldier.getPinfl(), soldier.getFullName(),
                soldier.getBirthDate(), soldier.getMilitaryUnit().getName(), soldier.getRegion().getName(),
                soldier.getDistrict().getName(),
                soldier.getSubdivision() == null ? null : soldier.getSubdivision().path());
    }

    static SoldierDto toDto(Soldier s) {
        Map<String, FieldSourceDto> sources = new LinkedHashMap<>();
        s.getFieldSources().forEach((field, info) ->
                sources.put(field, new FieldSourceDto(info.getSource(), info.getFilledBy(), info.getFilledAt())));
        return new SoldierDto(s.getId(), s.getPinfl(), s.getFullName(), s.getBirthDate(), s.getPassport(),
                s.getPhone(), s.getPhoneKinship() != null ? s.getPhoneKinship().getId() : null,
                s.getPhoneKinship() != null ? s.getPhoneKinship().getName() : null, s.getRegion().getId(),
                s.getRegion().getName(), s.getDistrict().getId(), s.getDistrict().getName(), s.getMahalla(),
                s.getStreet(), s.getHouse(), s.getApartment(), s.getMilitaryUnit().getId(),
                s.getMilitaryUnit().getName(), s.getConscriptionDate(), s.getServiceEndDate(),
                s.getGeneralEducation(), s.getProfessionalEducation(), s.getHigherEducation(),
                s.getPriorOccupation(), s.isNoPriorOccupation(), s.getTrainable(), s.getCycleYear(),
                s.getCertificates().stream().map(SoldierMapper::toCertificateDto).toList(),
                s.getAwards().stream().map(SoldierMapper::toAwardDto).toList(), sources,
                s.getSubdivision() == null ? null : s.getSubdivision().getId(),
                s.getSubdivision() == null ? null : s.getSubdivision().path());
    }

    private static CertificateDto toCertificateDto(SoldierCertificate c) {
        return new CertificateDto(c.getId(), c.getKind(),
                c.getDictionaryItem() != null ? c.getDictionaryItem().getId() : null,
                c.getDictionaryItem() != null ? c.getDictionaryItem().getName() : null, c.getTitle(), c.getLevel());
    }

    private static AwardDto toAwardDto(SoldierAward a) {
        return new AwardDto(a.getId(), a.getKind().getId(), a.getKind().getName(), a.getPlace());
    }
}
