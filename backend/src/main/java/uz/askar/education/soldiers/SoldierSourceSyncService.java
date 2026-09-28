package uz.askar.education.soldiers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.NotFoundException;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.organization.TerritorialDistrictRepository;
import uz.askar.education.security.CurrentUser;
import uz.askar.education.soldiers.integration.SoldierSourceClient;

/**
 * JShShIR qayta so'ralganda manba tizimdagi ma'lumot farq qilsa, avtomatik ustiga yozilmaydi:
 * farqlar ko'rsatiladi va xodim tasdiqlagan maydonlargina yangilanadi (TT 7.1-band).
 */
@Service
@RequiredArgsConstructor
public class SoldierSourceSyncService {

    public record FieldDiff(String field, String label, String current, String incoming) {
    }

    private final SoldierService soldierService;
    private final SoldierSourceClient sourceClient;
    private final uz.askar.education.integrations.IntegrationGateway gateway;
    private final RegionRepository regions;
    private final TerritorialDistrictRepository districts;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<FieldDiff> diff(Long soldierId) {
        Soldier soldier = soldierService.findInScope(soldierId);
        var incoming = gateway.call("MANBA", "JSHSHIR_REFRESH", String.valueOf(soldierId),
                        () -> sourceClient.findByPinfl(soldier.getPinfl()))
                .orElseThrow(() -> new NotFoundException("Manba tizimda askar topilmadi"));
        audit.record("JSHSHIR_LOOKUP", "Soldier", soldierId, "manba bilan solishtirish");
        List<FieldDiff> diffs = new ArrayList<>();
        add(diffs, "fullName", "F.I.Sh.", soldier.getFullName(), incoming.fullName());
        add(diffs, "birthDate", "Tug'ilgan sana", str(soldier.getBirthDate()), str(incoming.birthDate()));
        add(diffs, "passport", "Pasport", soldier.getPassport(), incoming.passport());
        add(diffs, "mahalla", "MFY", soldier.getMahalla(), incoming.mahalla());
        add(diffs, "street", "Ko'cha", soldier.getStreet(), incoming.street());
        add(diffs, "house", "Uy", soldier.getHouse(), incoming.house());
        add(diffs, "generalEducation", "Umumiy o'rta ta'lim", soldier.getGeneralEducation().name(),
                incoming.generalEducation());
        return diffs;
    }

    @Transactional
    public void apply(Long soldierId, Set<String> fields) {
        Soldier soldier = soldierService.findInScope(soldierId);
        var incoming = gateway.call("MANBA", "JSHSHIR_REFRESH", String.valueOf(soldierId),
                        () -> sourceClient.findByPinfl(soldier.getPinfl()))
                .orElseThrow(() -> new NotFoundException("Manba tizimda askar topilmadi"));
        LocalDateTime now = LocalDateTime.now();
        for (String field : fields) {
            switch (field) {
                case "fullName" -> soldier.setFullName(incoming.fullName());
                case "birthDate" -> soldier.setBirthDate(incoming.birthDate());
                case "passport" -> soldier.setPassport(incoming.passport());
                case "mahalla" -> soldier.setMahalla(incoming.mahalla());
                case "street" -> soldier.setStreet(incoming.street());
                case "house" -> soldier.setHouse(incoming.house());
                case "generalEducation" -> soldier.setGeneralEducation(GeneralEducation.valueOf(incoming.generalEducation()));
                default -> throw new NotFoundException("Noma'lum maydon: " + field);
            }
            soldier.getFieldSources().put(field, new FieldSourceInfo(FieldSource.INTEGRATION, currentUser.username(), now));
        }
        soldier.setUpdatedAt(now);
        audit.record("SOURCE_REFRESH", "Soldier", soldierId, "yangilangan maydonlar: " + fields);
    }

    private void add(List<FieldDiff> diffs, String field, String label, String current, String incoming) {
        if (!Objects.equals(current, incoming)) {
            diffs.add(new FieldDiff(field, label, current, incoming));
        }
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }
}
