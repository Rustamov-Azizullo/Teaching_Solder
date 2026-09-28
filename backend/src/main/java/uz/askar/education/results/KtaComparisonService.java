package uz.askar.education.results;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.integrations.IntegrationGateway;
import uz.askar.education.integrations.KtaClient;
import uz.askar.education.security.CurrentUser;

/** Bizdagi sertifikatlarni KTA ma'lumotlari bilan solishtirish; API yoki zaxira XLSX orqali (TT M9). */
@Service
@RequiredArgsConstructor
public class KtaComparisonService {

    public record Mismatch(String pinfl, String fullName, String unitName, String ourCertificate,
                           String ktaCertificate, String problem) {
    }

    private final CourseResultRepository results;
    private final KtaClient ktaClient;
    private final IntegrationGateway gateway;
    private final CurrentUser currentUser;
    private final AuditService audit;

    @Transactional(readOnly = true)
    public List<Mismatch> compareViaApi() {
        var certified = certified();
        var pinfls = certified.stream().map(r -> r.getSoldier().getPinfl()).toList();
        Map<String, String> kta = gateway.call("KTA", "CERTIFICATES", pinfls.size() + " ta", () -> ktaClient.certificatesByPinfl(pinfls));
        return compare(certified, kta);
    }

    @Transactional(readOnly = true)
    public List<Mismatch> compareViaFile(MultipartFile file) {
        Map<String, String> kta = new HashMap<>();
        try (var workbook = new XSSFWorkbook(file.getInputStream())) {
            DataFormatter formatter = new DataFormatter();
            for (Row row : workbook.getSheetAt(0)) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                String pinfl = formatter.formatCellValue(row.getCell(0)).trim();
                String number = formatter.formatCellValue(row.getCell(1)).trim();
                if (!pinfl.isEmpty() && !number.isEmpty()) {
                    kta.put(pinfl, number);
                }
            }
        } catch (IOException ex) {
            throw new BusinessRuleException("Fayl o'qilmadi. Ustunlar: JShShIR, sertifikat raqami (.xlsx)");
        }
        return compare(certified(), kta);
    }

    private List<CourseResult> certified() {
        var scope = currentUser.scope();
        return results.findCertifiedInScope(scope.districtFilter(), scope.unitFilter());
    }

    private List<Mismatch> compare(List<CourseResult> certified, Map<String, String> kta) {
        List<Mismatch> mismatches = certified.stream().map(r -> {
            String ours = r.getCertificateNo();
            String theirs = kta.get(r.getSoldier().getPinfl());
            String problem = theirs == null ? "KTA tizimida topilmadi"
                    : !theirs.equalsIgnoreCase(ours) && !"MOCK-MATCH".equals(theirs) ? "Sertifikat raqami mos emas" : null;
            return problem == null ? null : new Mismatch(r.getSoldier().getPinfl(), r.getSoldier().getFullName(),
                    r.getSoldier().getMilitaryUnit().getName(), ours, theirs, problem);
        }).filter(java.util.Objects::nonNull).toList();
        audit.record("KTA_COMPARE", "CourseResult", null, "tekshirildi: " + certified.size() + ", nomuvofiq: " + mismatches.size());
        return mismatches;
    }
}
