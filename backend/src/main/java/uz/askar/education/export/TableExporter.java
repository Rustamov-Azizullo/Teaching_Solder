package uz.askar.education.export;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** Bir xil {@link TableData} dan XLSX yoki PDF hosil qiladi. */
@Component
public class TableExporter {

    private static final int MAX_SHEET_NAME = 30;
    private static final int MAX_COLUMN_WIDTH_CHARS = 60;
    private static final int CHARS_TO_WIDTH_UNITS = 256;
    private static final float PDF_FONT_SIZE = 8f;
    private static final float PDF_TITLE_SIZE = 13f;

    public byte[] export(TableData data, ExportFormat format) {
        try {
            return format == ExportFormat.XLSX ? xlsx(data) : pdf(data);
        } catch (IOException ex) {
            throw new IllegalStateException("Faylni shakllantirib bo'lmadi", ex);
        }
    }

    private byte[] xlsx(TableData data) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String sheetName = data.title().length() > MAX_SHEET_NAME ? data.title().substring(0, MAX_SHEET_NAME)
                    : data.title();
            Sheet sheet = workbook.createSheet(sheetName.replaceAll("[\\\\/?*\\[\\]:]", " "));
            XSSFFont bold = workbook.createFont();
            bold.setBold(true);
            CellStyle header = workbook.createCellStyle();
            header.setFont(bold);
            int rowIndex = 0;
            sheet.createRow(rowIndex++).createCell(0).setCellValue(data.title());
            for (String line : data.meta()) {
                sheet.createRow(rowIndex++).createCell(0).setCellValue(line);
            }
            rowIndex++;
            Row headerRow = sheet.createRow(rowIndex++);
            for (int c = 0; c < data.headers().size(); c++) {
                var cell = headerRow.createCell(c);
                cell.setCellValue(data.headers().get(c));
                cell.setCellStyle(header);
            }
            for (var values : data.rows()) {
                Row row = sheet.createRow(rowIndex++);
                for (int c = 0; c < values.size(); c++) {
                    row.createCell(c).setCellValue(values.get(c));
                }
            }
            for (int c = 0; c < data.headers().size(); c++) {
                sheet.autoSizeColumn(c);
                sheet.setColumnWidth(c, Math.min(sheet.getColumnWidth(c), MAX_COLUMN_WIDTH_CHARS * CHARS_TO_WIDTH_UNITS));
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] pdf(TableData data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(data.headers().size() > 6 ? PageSize.A4.rotate() : PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);
        document.open();
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, PDF_TITLE_SIZE);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, PDF_FONT_SIZE);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, PDF_FONT_SIZE);
        document.add(new Paragraph(data.title(), titleFont));
        for (String line : data.meta()) {
            document.add(new Paragraph(line, textFont));
        }
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(data.headers().size());
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        for (String header : data.headers()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            table.addCell(cell);
        }
        for (var row : data.rows()) {
            for (String value : row) {
                table.addCell(new Phrase(value == null ? "" : value, textFont));
            }
        }
        document.add(table);
        document.close();
        return out.toByteArray();
    }
}
