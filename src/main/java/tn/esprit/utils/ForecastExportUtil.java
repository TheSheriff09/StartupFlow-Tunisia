package tn.esprit.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import tn.esprit.Services.ForecastRow;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ForecastExportUtil — professional export of financial forecast data.
 *
 * Two static entry points:
 *   {@link #exportToPDF(List, String, Window)}   — generates a styled PDF via OpenPDF
 *   {@link #exportToExcel(List, String, Window)} — generates a styled .xlsx via Apache POI
 *
 * Both methods:
 *   1. validate that the data list is non-empty
 *   2. open a FileChooser save dialog (on the JavaFX Application Thread)
 *   3. generate the file
 *   4. show a success or error Alert
 *
 * Call these methods only from the JavaFX Application Thread (e.g. a button handler).
 */
public final class ForecastExportUtil {

    // ── Column definitions ─────────────────────────────────────
    private static final String[] HEADERS = {
        "Month", "Revenue (TND)", "Expenses (TND)", "Net Profit (TND)", "Cumulative Profit (TND)"
    };

    // ── Brand colours ──────────────────────────────────────────
    private static final Color BRAND_GREEN   = new Color(5,   150, 105);  // #059669
    private static final Color BRAND_GREEN_L = new Color(209, 250, 229);  // light green
    private static final Color ROW_EVEN      = new Color(240, 253, 244);  // very light green
    private static final Color ROW_BORDER    = new Color(186, 230, 197);

    // Prevent instantiation
    private ForecastExportUtil() {}

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Prompts the user for a save location and writes a PDF report.
     *
     * @param rows        12-row forecast data (from GeminiForecastService.computeForecastRows)
     * @param startupName startup name for the report header (may be empty or null)
     * @param owner       parent window for the FileChooser and Alerts
     */
    public static void exportToPDF(List<ForecastRow> rows, String startupName, Window owner) {
        if (rows == null || rows.isEmpty()) {
            showWarning("No forecast data to export.\nPlease generate a forecast first.", owner);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Financial Forecast — PDF");
        fc.setInitialFileName("financial_forecast_" + LocalDate.now() + ".pdf");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));

        File file = fc.showSaveDialog(owner);
        if (file == null) return; // user cancelled

        try {
            generatePDF(rows, startupName, file);
            showSuccess("PDF exported successfully!\n\n" + file.getAbsolutePath(), owner);
        } catch (DocumentException | IOException e) {
            showError("PDF export failed:\n" + e.getMessage(), owner);
        }
    }

    /**
     * Prompts the user for a save location and writes an Excel (.xlsx) report.
     *
     * @param rows        12-row forecast data
     * @param startupName startup name for the report header (may be empty or null)
     * @param owner       parent window for the FileChooser and Alerts
     */
    public static void exportToExcel(List<ForecastRow> rows, String startupName, Window owner) {
        if (rows == null || rows.isEmpty()) {
            showWarning("No forecast data to export.\nPlease generate a forecast first.", owner);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Financial Forecast — Excel");
        fc.setInitialFileName("financial_forecast_" + LocalDate.now() + ".xlsx");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));

        File file = fc.showSaveDialog(owner);
        if (file == null) return; // user cancelled

        try {
            generateExcel(rows, startupName, file);
            showSuccess("Excel file exported successfully!\n\n" + file.getAbsolutePath(), owner);
        } catch (IOException e) {
            showError("Excel export failed:\n" + e.getMessage(), owner);
        }
    }

    // ─────────────────────────────────────────────────────────
    // PDF generation (OpenPDF)
    // ─────────────────────────────────────────────────────────

    private static void generatePDF(List<ForecastRow> rows, String startupName, File file)
            throws DocumentException, IOException {

        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // ── Fonts ─────────────────────────────────────────────
        Font titleFont  = new Font(Font.HELVETICA, 20, Font.BOLD,   BRAND_GREEN);
        Font subFont    = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(70, 70,  70));
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD,   Color.WHITE);
        Font rowFont    = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(35, 35,  35));
        Font totalFont  = new Font(Font.HELVETICA, 10, Font.BOLD,   BRAND_GREEN);
        Font footerFont = new Font(Font.HELVETICA,  9, Font.ITALIC, new Color(140, 140, 140));

        // ── Title ─────────────────────────────────────────────
        Paragraph title = new Paragraph("Financial Forecast Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        doc.add(title);

        // ── Horizontal rule (simulated with a thin table) ─────
        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        rule.setSpacingAfter(14);
        PdfPCell ruleLine = new PdfPCell();
        ruleLine.setFixedHeight(2);
        ruleLine.setBackgroundColor(BRAND_GREEN);
        ruleLine.setBorder(Rectangle.NO_BORDER);
        rule.addCell(ruleLine);
        doc.add(rule);

        // ── Meta info ─────────────────────────────────────────
        String dateStr = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        doc.add(new Paragraph("Generated: " + dateStr, subFont));
        if (startupName != null && !startupName.isBlank()) {
            doc.add(new Paragraph("Startup:   " + startupName, subFont));
        }
        doc.add(new Paragraph("Projection Period: 12 Months (Monthly Compound Growth)", subFont));
        doc.add(Chunk.NEWLINE);

        // ── Data table ────────────────────────────────────────
        float[] colWidths = { 0.55f, 1.4f, 1.4f, 1.4f, 1.65f };
        PdfPTable table = new PdfPTable(colWidths);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // Header row
        for (String h : HEADERS) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BRAND_GREEN);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(8);
            cell.setPaddingBottom(8);
            cell.setPaddingLeft(6);
            cell.setPaddingRight(6);
            cell.setBorderColor(new Color(3, 120, 84));
            table.addCell(cell);
        }

        // Data rows
        double totalRevenue = 0, totalExpenses = 0, totalNetProfit = 0;

        for (ForecastRow row : rows) {
            boolean even = (row.getMonth() % 2 == 0);
            Color   bg   = even ? ROW_EVEN : Color.WHITE;

            addPdfCell(table, String.valueOf(row.getMonth()),        rowFont, bg, Element.ALIGN_CENTER);
            addPdfCell(table, fmt(row.getRevenue()),                 rowFont, bg, Element.ALIGN_RIGHT);
            addPdfCell(table, fmt(row.getExpenses()),                rowFont, bg, Element.ALIGN_RIGHT);
            addPdfCell(table, fmtSigned(row.getNetProfit()),         rowFont, bg, Element.ALIGN_RIGHT);
            addPdfCell(table, fmtSigned(row.getCumulativeProfit()),  rowFont, bg, Element.ALIGN_RIGHT);

            totalRevenue   += row.getRevenue();
            totalExpenses  += row.getExpenses();
            totalNetProfit += row.getNetProfit();
        }

        // Totals row
        double finalCumulative = rows.get(rows.size() - 1).getCumulativeProfit();
        addPdfCell(table, "TOTAL",                    totalFont, BRAND_GREEN_L, Element.ALIGN_CENTER);
        addPdfCell(table, fmt(totalRevenue),           totalFont, BRAND_GREEN_L, Element.ALIGN_RIGHT);
        addPdfCell(table, fmt(totalExpenses),          totalFont, BRAND_GREEN_L, Element.ALIGN_RIGHT);
        addPdfCell(table, fmtSigned(totalNetProfit),   totalFont, BRAND_GREEN_L, Element.ALIGN_RIGHT);
        addPdfCell(table, fmtSigned(finalCumulative),  totalFont, BRAND_GREEN_L, Element.ALIGN_RIGHT);

        doc.add(table);
        doc.add(Chunk.NEWLINE);

        // ── Footer ────────────────────────────────────────────
        Paragraph footer = new Paragraph(
                "This report was generated by the Startup Flow application  ·  " + dateStr,
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
    }

    /** Adds a styled data cell to the PDF table. */
    private static void addPdfCell(PdfPTable table, String text, Font font,
                                   Color bg, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setPaddingLeft(6);
        cell.setPaddingRight(6);
        cell.setBorderColor(ROW_BORDER);
        table.addCell(cell);
    }

    // ─────────────────────────────────────────────────────────
    // Excel generation (Apache POI)
    // ─────────────────────────────────────────────────────────

    private static void generateExcel(List<ForecastRow> rows, String startupName, File file)
            throws IOException {

        try (XSSFWorkbook wb  = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            XSSFSheet sheet = wb.createSheet("Financial Forecast");

            int cursor = 0; // current row index

            // ── Title row ─────────────────────────────────────
            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont      titleFont  = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 15);
            titleFont.setColor(new XSSFColor(new byte[]{(byte) 5, (byte) 150, (byte) 105}, null));
            titleStyle.setFont(titleFont);

            XSSFRow titleRow = sheet.createRow(cursor++);
            XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Financial Forecast Report");
            titleCell.setCellStyle(titleStyle);

            // ── Meta rows ─────────────────────────────────────
            XSSFCellStyle metaStyle = wb.createCellStyle();
            XSSFFont      metaFont  = wb.createFont();
            metaFont.setColor(new XSSFColor(new byte[]{(byte) 80, (byte) 80, (byte) 80}, null));
            metaStyle.setFont(metaFont);

            String dateStr = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

            XSSFRow dateRow = sheet.createRow(cursor++);
            addExcelMeta(dateRow, "Generated:", dateStr, metaStyle, wb);

            if (startupName != null && !startupName.isBlank()) {
                XSSFRow snRow = sheet.createRow(cursor++);
                addExcelMeta(snRow, "Startup:", startupName, metaStyle, wb);
            }

            XSSFRow periRow = sheet.createRow(cursor++);
            addExcelMeta(periRow, "Projection:", "12 Months (Monthly Compound Growth)", metaStyle, wb);

            cursor++; // blank spacer row

            // ── Header row ────────────────────────────────────
            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 5, (byte) 150, (byte) 105}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.MEDIUM);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(new XSSFColor(new byte[]{(byte)255,(byte)255,(byte)255}, null));
            headerStyle.setFont(headerFont);
            headerStyle.setWrapText(true);

            XSSFRow headerRow = sheet.createRow(cursor++);
            headerRow.setHeight((short) 600); // slightly taller
            for (int i = 0; i < HEADERS.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Shared number format ──────────────────────────
            XSSFDataFormat df    = wb.createDataFormat();
            short           numFmt = df.getFormat("#,##0.00");

            // Build two alternating row styles (even / odd)
            XSSFCellStyle evenBase = makeDataStyle(wb,
                    new byte[]{(byte) 240, (byte) 253, (byte) 244});  // light green
            XSSFCellStyle oddBase  = makeDataStyle(wb,
                    new byte[]{(byte) 255, (byte) 255, (byte) 255});  // white

            XSSFCellStyle evenNum = (XSSFCellStyle) wb.createCellStyle();
            evenNum.cloneStyleFrom(evenBase);
            evenNum.setDataFormat(numFmt);
            evenNum.setAlignment(HorizontalAlignment.RIGHT);

            XSSFCellStyle oddNum  = (XSSFCellStyle) wb.createCellStyle();
            oddNum.cloneStyleFrom(oddBase);
            oddNum.setDataFormat(numFmt);
            oddNum.setAlignment(HorizontalAlignment.RIGHT);

            // ── Data rows ─────────────────────────────────────
            double totRevenue = 0, totExpenses = 0, totNetProfit = 0;

            for (ForecastRow fr : rows) {
                boolean even = (fr.getMonth() % 2 == 0);
                XSSFRow row = sheet.createRow(cursor++);

                XSSFCell c0 = row.createCell(0); c0.setCellValue(fr.getMonth());
                c0.setCellStyle(even ? evenBase : oddBase);

                XSSFCell c1 = row.createCell(1); c1.setCellValue(fr.getRevenue());
                c1.setCellStyle(even ? evenNum : oddNum);

                XSSFCell c2 = row.createCell(2); c2.setCellValue(fr.getExpenses());
                c2.setCellStyle(even ? evenNum : oddNum);

                XSSFCell c3 = row.createCell(3); c3.setCellValue(fr.getNetProfit());
                c3.setCellStyle(even ? evenNum : oddNum);

                XSSFCell c4 = row.createCell(4); c4.setCellValue(fr.getCumulativeProfit());
                c4.setCellStyle(even ? evenNum : oddNum);

                totRevenue   += fr.getRevenue();
                totExpenses  += fr.getExpenses();
                totNetProfit += fr.getNetProfit();
            }

            // ── Totals row ────────────────────────────────────
            XSSFCellStyle totBase = wb.createCellStyle();
            totBase.setFillForegroundColor(
                    new XSSFColor(new byte[]{(byte) 209, (byte) 250, (byte) 229}, null));
            totBase.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totBase.setBorderTop(BorderStyle.MEDIUM);
            totBase.setBorderBottom(BorderStyle.MEDIUM);
            totBase.setBorderLeft(BorderStyle.THIN);
            totBase.setBorderRight(BorderStyle.THIN);

            XSSFFont totFont = wb.createFont();
            totFont.setBold(true);
            totFont.setColor(new XSSFColor(new byte[]{(byte) 5, (byte) 120, (byte) 80}, null));
            totBase.setFont(totFont);

            XSSFCellStyle totNum = (XSSFCellStyle) wb.createCellStyle();
            totNum.cloneStyleFrom(totBase);
            totNum.setDataFormat(numFmt);
            totNum.setAlignment(HorizontalAlignment.RIGHT);

            double finalCum = rows.get(rows.size() - 1).getCumulativeProfit();
            XSSFRow totRow = sheet.createRow(cursor);
            XSSFCell tl = totRow.createCell(0); tl.setCellValue("TOTAL");      tl.setCellStyle(totBase);
            XSSFCell t1 = totRow.createCell(1); t1.setCellValue(totRevenue);   t1.setCellStyle(totNum);
            XSSFCell t2 = totRow.createCell(2); t2.setCellValue(totExpenses);  t2.setCellStyle(totNum);
            XSSFCell t3 = totRow.createCell(3); t3.setCellValue(totNetProfit); t3.setCellStyle(totNum);
            XSSFCell t4 = totRow.createCell(4); t4.setCellValue(finalCum);     t4.setCellStyle(totNum);

            // ── Auto-size columns ─────────────────────────────
            for (int col = 0; col < HEADERS.length; col++) {
                sheet.autoSizeColumn(col);
                // add a small padding after autoSize
                sheet.setColumnWidth(col, sheet.getColumnWidth(col) + 512);
            }

            wb.write(fos);
        }
    }

    /**
     * Creates a base cell style for data rows with the given background colour.
     * Borders are set; font is default; no number format (use cloneStyleFrom to add).
     */
    private static XSSFCellStyle makeDataStyle(XSSFWorkbook wb, byte[] rgb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(rgb, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /** Writes a two-cell label/value meta row. */
    private static void addExcelMeta(XSSFRow row, String label, String value,
                                     XSSFCellStyle style, XSSFWorkbook wb) {
        XSSFCellStyle labelStyle = wb.createCellStyle();
        labelStyle.cloneStyleFrom(style);
        XSSFFont boldMetaFont = wb.createFont();
        boldMetaFont.setBold(true);
        boldMetaFont.setColor(new XSSFColor(new byte[]{(byte) 50, (byte) 50, (byte) 50}, null));
        labelStyle.setFont(boldMetaFont);

        XSSFCell c0 = row.createCell(0);
        c0.setCellValue(label);
        c0.setCellStyle(labelStyle);

        XSSFCell c1 = row.createCell(1);
        c1.setCellValue(value);
        c1.setCellStyle(style);
    }

    // ─────────────────────────────────────────────────────────
    // Number formatting helpers
    // ─────────────────────────────────────────────────────────

    /** Formats a value with comma grouping: 12500.00 → "12,500.00" */
    private static String fmt(double value) {
        return String.format("%,.2f", value);
    }

    /** Formats with sign: positive → "+12,500.00", negative → "-12,500.00" */
    private static String fmtSigned(double value) {
        return (value >= 0 ? "+" : "-") + String.format("%,.2f", Math.abs(value));
    }

    // ─────────────────────────────────────────────────────────
    // Alert helpers (must be called on the JavaFX Application Thread)
    // ─────────────────────────────────────────────────────────

    private static void showSuccess(String message, Window owner) {
        AlertUtil.showSuccess("\u2705  Export Successful", message, owner);
    }

    private static void showError(String message, Window owner) {
        AlertUtil.showError("Export Failed", message, owner);
    }

    private static void showWarning(String message, Window owner) {
        AlertUtil.showWarning("Nothing to Export", message, owner);
    }
}

