package uz.askar.education.soldiers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.common.PageResponse;
import uz.askar.education.dictionaries.DictionaryItem;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.organization.MilitaryUnit;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.Region;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.organization.TerritorialDistrict;
import uz.askar.education.organization.TerritorialDistrictRepository;
import uz.askar.education.security.AccessScope;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.soldiers.SoldierDtos.AwardInput;
import uz.askar.education.soldiers.SoldierDtos.CertificateInput;
import uz.askar.education.soldiers.SoldierDtos.SoldierDto;
import uz.askar.education.soldiers.SoldierDtos.SoldierRequest;
import uz.askar.education.soldiers.SoldierDtos.SoldierSummary;
import uz.askar.education.soldiers.SoldierDtos.SourceLookupResponse;
import uz.askar.education.soldiers.integration.SoldierSourceClient;
import uz.askar.education.soldiers.integration.SourceSoldierData;
import uz.askar.education.soldiers.integration.SourceSystemUnavailableException;

@Service
@RequiredArgsConstructor
public class SoldierService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SoldierRepository soldiers;
    private final MilitaryUnitRepository militaryUnits;
    private final RegionRepository regions;
    private final TerritorialDistrictRepository districts;
    private final DictionaryItemRepository dictionaryItems;
    private final SoldierSourceClient sourceClient;
    private final uz.askar.education.cycles.CycleService cycleService;
    private final uz.askar.education.integrations.IntegrationGateway gateway;
    private final uz.askar.education.organization.SubdivisionService subdivisionService;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public PageResponse<SoldierSummary> search(String query, Long districtId, Long unitId, Long subdivisionId,
                                               int page, int size) {
        AccessScope scope = currentUser.scope();
        Long districtFilter = scope.districtFilter() != null ? scope.districtFilter() : districtId;
        Long unitFilter = scope.unitFilter() != null ? scope.unitFilter() : unitId;
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, MAX_PAGE_SIZE)), Sort.by("fullName"));
        var subdivisionIds = subdivisionId == null
                ? java.util.Set.of(-1L) : subdivisionService.withDescendants(subdivisionId);
        var result = soldiers.search(query == null ? "" : query.trim(), districtFilter, unitFilter,
                subdivisionId != null, subdivisionIds, pageable);
        return PageResponse.from(result, SoldierMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public SoldierDto get(Long id) {
        return SoldierMapper.toDto(findInScope(id));
    }

    @Transactional
    public SoldierDto create(SoldierRequest request) {
        cycleService.requireCurrentOpen();
        if (soldiers.existsByPinfl(request.pinfl())) {
            throw new BusinessRuleException("Bu JShShIR bilan askar allaqachon ro'yxatga olingan");
        }
        Soldier soldier = new Soldier();
        soldier.setPinfl(request.pinfl());
        soldier.setCycleYear(LocalDate.now().getYear());
        soldier.setCreatedAt(LocalDateTime.now());
        apply(soldier, request);
        Soldier saved = soldiers.save(soldier);
        audit.record("CREATE", "Soldier", saved.getId(), "JShShIR=" + saved.getPinfl());
        return SoldierMapper.toDto(saved);
    }

    @Transactional
    public SoldierDto update(Long id, SoldierRequest request) {
        Soldier soldier = findInScope(id);
        if (!soldier.getPinfl().equals(request.pinfl())) {
            throw new BusinessRuleException("JShShIR o'zgartirilmaydi");
        }
        apply(soldier, request);
        audit.record("UPDATE", "Soldier", id, "Yig'ma jild yangilandi");
        return SoldierMapper.toDto(soldier);
    }

    /** JShShIR bo'yicha manba tizimga so'rov; har bir so'rov audit jurnaliga yoziladi (TT 10-bo'lim, 6-band). */
    @Transactional
    public SourceLookupResponse lookupInSource(String pinfl) {
        var existing = soldiers.findByPinfl(pinfl);
        if (existing.isPresent()) {
            Soldier found = existing.get();
            audit.record("JSHSHIR_LOOKUP", "Soldier", found.getId(), "Askar tizimda mavjud");
            boolean isInScope = currentUser.scope().covers(found.getMilitaryUnit().getMilitaryDistrict().getId(),
                    found.getMilitaryUnit().getId());
            if (!isInScope) {
                throw new BusinessRuleException("Bu JShShIR bilan askar boshqa hududda ro'yxatga olingan");
            }
            return SourceLookupResponse.notFound(found.getId());
        }
        try {
            var data = gateway.call("MANBA", "JSHSHIR", maskPinfl(pinfl), () -> sourceClient.findByPinfl(pinfl));
            audit.record("JSHSHIR_LOOKUP", "SourceSystem", pinfl, data.isPresent() ? "topildi" : "topilmadi");
            return data.map(this::toLookupResponse).orElseGet(() -> SourceLookupResponse.notFound(null));
        } catch (SourceSystemUnavailableException ex) {
            audit.record("JSHSHIR_LOOKUP", "SourceSystem", pinfl, "manba tizim javob bermadi");
            throw ex;
        }
    }

    public Soldier findInScope(Long id) {
        Soldier soldier = soldiers.findById(id).orElseThrow(() -> new NotFoundException("Askar topilmadi"));
        currentUser.scope().require(soldier.getMilitaryUnit().getMilitaryDistrict().getId(),
                soldier.getMilitaryUnit().getId());
        return soldier;
    }

    private String maskPinfl(String pinfl) {
        return pinfl.substring(0, 3) + "********" + pinfl.substring(pinfl.length() - 3);
    }

    public SourceLookupResponse toLookupResponse(SourceSoldierData data) {
        Region region = regions.findAllByOrderByNameAsc().stream()
                .filter(r -> r.getName().equals(data.regionName())).findFirst().orElse(null);
        TerritorialDistrict district = region == null ? null : districts.findByRegionIdOrderByNameAsc(region.getId())
                .stream().filter(d -> d.getName().equals(data.districtName())).findFirst().orElse(null);
        return new SourceLookupResponse(true, null, data.fullName(), data.birthDate(), data.passport(),
                region != null ? region.getId() : null, district != null ? district.getId() : null, data.mahalla(),
                data.street(), data.house(), GeneralEducation.valueOf(data.generalEducation()));
    }

    private void apply(Soldier soldier, SoldierRequest r) {
        MilitaryUnit unit = militaryUnits.findById(r.militaryUnitId())
                .orElseThrow(() -> new NotFoundException("Harbiy qism topilmadi"));
        currentUser.scope().require(unit.getMilitaryDistrict().getId(), unit.getId());
        TerritorialDistrict district = districts.findById(r.districtId())
                .orElseThrow(() -> new NotFoundException("Tuman topilmadi"));
        if (!district.getRegion().getId().equals(r.regionId())) {
            throw new BusinessRuleException("Tanlangan tuman ko'rsatilgan viloyatga tegishli emas");
        }
        soldier.setFullName(r.fullName());
        soldier.setBirthDate(r.birthDate());
        soldier.setPassport(r.passport());
        soldier.setPhone(r.phone());
        soldier.setPhoneKinship(dictionaryItem(r.phoneKinshipId(), DictionaryType.KINSHIP));
        soldier.setRegion(district.getRegion());
        soldier.setDistrict(district);
        soldier.setMahalla(r.mahalla());
        soldier.setStreet(r.street());
        soldier.setHouse(r.house());
        soldier.setApartment(r.apartment());
        soldier.setMilitaryUnit(unit);
        soldier.setSubdivision(resolveSubdivision(r.subdivisionId(), unit));
        soldier.setConscriptionDate(r.conscriptionDate());
        soldier.setServiceEndDate(r.serviceEndDate());
        soldier.setGeneralEducation(r.generalEducation());
        soldier.setProfessionalEducation(r.professionalEducation());
        soldier.setHigherEducation(r.higherEducation());
        soldier.setNoPriorOccupation(r.noPriorOccupation());
        soldier.setPriorOccupation(r.noPriorOccupation() ? null : r.priorOccupation());
        soldier.setTrainable(r.trainable());
        soldier.setUpdatedAt(LocalDateTime.now());
        replaceCertificates(soldier, r.certificates());
        replaceAwards(soldier, r.awards());
        recordFieldSources(soldier, r.fieldSources());
    }

    private uz.askar.education.organization.Subdivision resolveSubdivision(Long id, MilitaryUnit unit) {
        if (id == null) {
            return null;
        }
        var subdivision = subdivisionService.findInScope(id);
        if (!subdivision.getMilitaryUnit().getId().equals(unit.getId())) {
            throw new BusinessRuleException("Bo'linma tanlangan harbiy qismga tegishli emas");
        }
        return subdivision;
    }

    private void replaceCertificates(Soldier soldier, List<CertificateInput> inputs) {
        soldier.getCertificates().clear();
        if (inputs == null) {
            return;
        }
        for (CertificateInput input : inputs) {
            SoldierCertificate certificate = new SoldierCertificate();
            certificate.setSoldier(soldier);
            certificate.setKind(input.kind());
            certificate.setLevel(input.level());
            certificate.setTitle(input.title());
            certificate.setDictionaryItem(input.dictionaryItemId() == null ? null
                    : dictionaryItem(input.dictionaryItemId(), dictionaryTypeFor(input.kind())));
            requireCertificateDetail(certificate);
            soldier.getCertificates().add(certificate);
        }
    }

    private void requireCertificateDetail(SoldierCertificate certificate) {
        boolean isProfession = certificate.getKind() == CertificateKind.PROFESSION;
        boolean hasDetail = isProfession
                ? certificate.getTitle() != null && !certificate.getTitle().isBlank()
                : certificate.getDictionaryItem() != null;
        if (!hasDetail) {
            throw new BusinessRuleException("Sertifikat tafsiloti ko'rsatilmagan (til, kasb yoki fan)");
        }
    }

    private DictionaryType dictionaryTypeFor(CertificateKind kind) {
        return switch (kind) {
            case LANGUAGE -> DictionaryType.LANGUAGE;
            case SUBJECT -> DictionaryType.SUBJECT;
            case PROFESSION -> DictionaryType.PROFESSION;
        };
    }

    private void replaceAwards(Soldier soldier, List<AwardInput> inputs) {
        soldier.getAwards().clear();
        if (inputs == null) {
            return;
        }
        for (AwardInput input : inputs) {
            SoldierAward award = new SoldierAward();
            award.setSoldier(soldier);
            award.setKind(dictionaryItem(input.kindId(), DictionaryType.AWARD_KIND));
            award.setPlace(input.place());
            soldier.getAwards().add(award);
        }
    }

    /** Frontend "integratsiya" deb belgilagan maydonlar INTEGRATION, qolganlari MANUAL bo'lib yoziladi. */
    private void recordFieldSources(Soldier soldier, Map<String, FieldSource> requested) {
        if (requested == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        requested.forEach((field, source) ->
                soldier.getFieldSources().put(field, new FieldSourceInfo(source, currentUser.username(), now)));
    }

    private DictionaryItem dictionaryItem(Long id, DictionaryType expectedType) {
        if (id == null) {
            return null;
        }
        return dictionaryItems.findById(id)
                .filter(item -> item.getType() == expectedType)
                .orElseThrow(() -> new NotFoundException("Ma'lumotnoma yozuvi topilmadi: " + expectedType));
    }
}
