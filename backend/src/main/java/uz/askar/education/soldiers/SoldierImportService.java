package uz.askar.education.soldiers;

import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.askar.education.audit.AuditService;
import uz.askar.education.common.BusinessRuleException;
import uz.askar.education.dictionaries.DictionaryItemRepository;
import uz.askar.education.dictionaries.DictionaryType;
import uz.askar.education.organization.MilitaryUnitRepository;
import uz.askar.education.organization.RegionRepository;
import uz.askar.education.organization.TerritorialDistrictRepository;
import uz.askar.education.soldiers.SoldierDtos.SoldierRequest;

/** XLSX shablon orqali ommaviy import (TT 7.3): har bir qator alohida tekshiriladi, xatolar hisoboti qaytariladi. */
@Service
@RequiredArgsConstructor
public class SoldierImportService {

    public static final List<String> COLUMNS = List.of("JShShIR", "F.I.Sh.", "Tug'ilgan sana (KK.OO.YYYY)",
            "Pasport (AA1234567)", "Telefon (+998...)", "Qarindoshlik kodi (FATHER...)", "Viloyat", "Tuman/shahar",
            "MFY", "Ko'cha", "Uy", "Harbiy qism kodi", "Umumiy o'rta ta'lim (SCHOOL/LYCEUM)");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final int MAX_ROWS = 2000;

    public record RowError(int row, String message) {
    }

    public record ImportResult(int imported, List<RowError> errors) {
    }

    private final SoldierService soldierService;
    private final Validator validator;
    private final MilitaryUnitRepository units;
    private final RegionRepository regions;
    private final TerritorialDistrictRepository districts;
    private final DictionaryItemRepository dictionaryItems;
    private final AuditService audit;

    public byte[] template() {
        try (Workbook workbook = new XSSFWorkbook(); var out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Askarlar");
            Row header = sheet.createRow(0);
            for (int i = 0; i < COLUMNS.size(); i++) {
                header.createCell(i).setCellValue(COLUMNS.get(i));
                sheet.setColumnWidth(i, 22 * 256);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Shablonni shakllantirib bo'lmadi", ex);
        }
    }

    public ImportResult importFile(MultipartFile file) {
        List<RowError> errors = new ArrayList<>();
        int imported = 0;
        try (InputStream in = file.getInputStream(); Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() > MAX_ROWS) {
                throw new BusinessRuleException("Bir faylda " + MAX_ROWS + " tadan ortiq qator bo'lmasligi kerak");
            }
            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlank(row, formatter)) {
                    continue;
                }
                try {
                    soldierService.create(toRequest(row, formatter));
                    imported++;
                } catch (RuntimeException ex) {
                    errors.add(new RowError(r + 1, ex.getMessage() == null ? "Xatolik" : ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            throw new BusinessRuleException("Fayl o'qilmadi. .xlsx shablondan foydalaning");
        }
        audit.record("IMPORT", "Soldier", null, "yuklandi: " + imported + ", xato: " + errors.size());
        return new ImportResult(imported, errors);
    }

    private SoldierRequest toRequest(Row row, DataFormatter f) {
        String[] c = new String[COLUMNS.size()];
        for (int i = 0; i < c.length; i++) {
            c[i] = f.formatCellValue(row.getCell(i)).trim();
        }
        var unit = units.findByCode(c[11]).orElseThrow(() -> new BusinessRuleException("Harbiy qism kodi topilmadi: " + c[11]));
        var region = regions.findAllByOrderByNameAsc().stream().filter(x -> x.getName().equalsIgnoreCase(c[6]))
                .findFirst().orElseThrow(() -> new BusinessRuleException("Viloyat topilmadi: " + c[6]));
        var district = districts.findByRegionIdOrderByNameAsc(region.getId()).stream()
                .filter(x -> x.getName().equalsIgnoreCase(c[7])).findFirst()
                .orElseThrow(() -> new BusinessRuleException("Tuman topilmadi: " + c[7]));
        var kinship = dictionaryItems.findByTypeOrderBySortOrderAscNameAsc(DictionaryType.KINSHIP).stream()
                .filter(x -> x.getCode().equalsIgnoreCase(c[5])).findFirst()
                .orElseThrow(() -> new BusinessRuleException("Qarindoshlik kodi topilmadi: " + c[5]));
        GeneralEducation education;
        try {
            education = GeneralEducation.valueOf(c[12].toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Umumiy ta'lim SCHOOL yoki LYCEUM bo'lishi kerak");
        }
        SoldierRequest request = new SoldierRequest(c[0], c[1], parseDate(c[2]), c[3].toUpperCase(), c[4],
                kinship.getId(), region.getId(), district.getId(), c[8], c[9], c[10], null, unit.getId(), null, null,
                education, null, null, null, false, null, List.of(), List.of(), java.util.Map.of(), null);
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BusinessRuleException(violations.stream().map(v -> v.getMessage()).distinct()
                    .collect(Collectors.joining("; ")));
        }
        return request;
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new BusinessRuleException("Sana formati KK.OO.YYYY bo'lishi kerak: " + text);
        }
    }

    private boolean isBlank(Row row, DataFormatter formatter) {
        for (int i = 0; i < COLUMNS.size(); i++) {
            if (!formatter.formatCellValue(row.getCell(i)).isBlank()) {
                return false;
            }
        }
        return true;
    }
}
