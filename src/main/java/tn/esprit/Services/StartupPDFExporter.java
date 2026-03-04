package tn.esprit.Services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.entities.Startup;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * StartupPDFExporter — Professional PDF export for Startup and BusinessPlan entities.
 *
 * Uses OpenPDF (free fork of iText 2.x).
 * Two public entry points:
 *   {@link #exportStartupToPDF(Startup, List, double, File)}
 *   {@link #exportBusinessPlanToPDF(BusinessPlan, double, File)}
 */
public final class StartupPDFExporter {

    // ── Brand colours ──────────────────────────────────────────
    private static final Color BRAND_PURPLE     = new Color(109, 40, 217);  // #6d28d9
    private static final Color BRAND_PURPLE_L   = new Color(237, 233, 254); // #ede9fe
    private static final Color ROW_EVEN         = new Color(245, 243, 255);
    private static final Color ROW_BORDER       = new Color(196, 181, 253);
    private static final Color TEXT_DARK        = new Color(30, 20, 50);
    private static final Color TEXT_MUTED       = new Color(100, 116, 139);

    // ── Fonts ──────────────────────────────────────────────────
    private static final Font TITLE_FONT    = new Font(Font.HELVETICA, 22, Font.BOLD, BRAND_PURPLE);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, BRAND_PURPLE);
    private static final Font SECTION_FONT  = new Font(Font.HELVETICA, 12, Font.BOLD, BRAND_PURPLE);
    private static final Font BODY_FONT     = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_DARK);
    private static final Font LABEL_FONT    = new Font(Font.HELVETICA, 10, Font.BOLD, TEXT_MUTED);
    private static final Font SMALL_FONT    = new Font(Font.HELVETICA, 8, Font.ITALIC, TEXT_MUTED);
    private static final Font BADGE_FONT    = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private StartupPDFExporter() {}

    // ─────────────────────────────────────────────────────────
    // Startup PDF
    // ─────────────────────────────────────────────────────────

    /**
     * Exports a full Startup report including its business plans,
     * investment score, and risk recommendation.
     *
     * @param startup the startup entity
     * @param plans   list of business plans for this startup
     * @param score   investment score (0–100)
     * @param file    destination PDF file
     */
    public static void exportStartupToPDF(Startup startup, List<BusinessPlan> plans,
                                          double score, File file)
            throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // ── Header ──
        addHeader(doc, "Startup Report", startup.getName());

        // ── Startup Details ──
        addSection(doc, "Startup Details");
        PdfPTable details = new PdfPTable(2);
        details.setWidthPercentage(100);
        details.setWidths(new float[]{30, 70});
        details.setSpacingBefore(6);

        addRow(details, "Name", str(startup.getName()));
        addRow(details, "Sector", str(startup.getSector()));
        addRow(details, "Stage", str(startup.getStage()));
        addRow(details, "Status", str(startup.getStatus()));
        addRow(details, "Funding Amount",
                startup.getFundingAmount() != null
                        ? String.format("$%,.2f", startup.getFundingAmount()) : "N/A");
        addRow(details, "KPI Score",
                startup.getKpiScore() != null
                        ? String.format("%.1f / 10", startup.getKpiScore()) : "N/A");
        addRow(details, "Creation Date",
                startup.getCreationDate() != null
                        ? startup.getCreationDate().format(DATE_FMT) : "N/A");
        addRow(details, "Incubator", str(startup.getIncubatorProgram()));
        addRow(details, "Description", str(startup.getDescription()));
        doc.add(details);

        // ── Investment Score ──
        addSection(doc, "Investment Analysis");
        String band     = InvestmentScorer.band(score);
        String reco     = score >= 70 ? "Strong Investment Candidate"
                        : score >= 40 ? "Moderate — Needs Due Diligence"
                        : "High Risk — Proceed with Caution";

        PdfPTable scoreTable = new PdfPTable(2);
        scoreTable.setWidthPercentage(100);
        scoreTable.setWidths(new float[]{30, 70});
        scoreTable.setSpacingBefore(6);
        addRow(scoreTable, "Investment Score", String.format("%.0f / 100", score));
        addRow(scoreTable, "Risk Band", band);
        addRow(scoreTable, "Recommendation", reco);
        doc.add(scoreTable);

        // ── Business Plans Summary ──
        if (plans != null && !plans.isEmpty()) {
            addSection(doc, "Business Plans (" + plans.size() + ")");

            for (int i = 0; i < plans.size(); i++) {
                BusinessPlan bp = plans.get(i);
                doc.add(new Paragraph((i + 1) + ". " + str(bp.getTitle()),
                        SUBTITLE_FONT));
                doc.add(Chunk.NEWLINE);

                PdfPTable bpTable = new PdfPTable(2);
                bpTable.setWidthPercentage(100);
                bpTable.setWidths(new float[]{30, 70});
                bpTable.setSpacingBefore(4);

                addRow(bpTable, "Status", str(bp.getStatus()));
                addRow(bpTable, "Funding Required",
                        bp.getFundingRequired() != null
                                ? String.format("$%,.2f", bp.getFundingRequired()) : "N/A");
                addRow(bpTable, "Timeline", str(bp.getTimeline()));
                addRow(bpTable, "Plan Score",
                        String.format("%.0f / 100", InvestmentScorer.scorePlan(bp)));

                if (bp.getDescription() != null && !bp.getDescription().isBlank())
                    addRow(bpTable, "Description", bp.getDescription());
                if (bp.getMarketAnalysis() != null && !bp.getMarketAnalysis().isBlank())
                    addRow(bpTable, "Market Analysis", bp.getMarketAnalysis());
                if (bp.getFinancialForecast() != null && !bp.getFinancialForecast().isBlank())
                    addRow(bpTable, "Financial Forecast", bp.getFinancialForecast());

                doc.add(bpTable);
                doc.add(Chunk.NEWLINE);
            }
        }

        // ── Footer ──
        addFooter(doc);
        doc.close();
    }

    // ─────────────────────────────────────────────────────────
    // BusinessPlan PDF
    // ─────────────────────────────────────────────────────────

    /**
     * Exports a full BusinessPlan report with structured sections and scoring.
     *
     * @param bp    the business plan entity
     * @param score the plan's investment score (from InvestmentScorer.scorePlan)
     * @param file  destination PDF file
     */
    public static void exportBusinessPlanToPDF(BusinessPlan bp, double score, File file)
            throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // ── Header ──
        addHeader(doc, "Business Plan Report", bp.getTitle());

        // ── Plan Overview ──
        addSection(doc, "Plan Overview");

        PdfPTable overview = new PdfPTable(2);
        overview.setWidthPercentage(100);
        overview.setWidths(new float[]{30, 70});
        overview.setSpacingBefore(6);

        addRow(overview, "Title", str(bp.getTitle()));
        addRow(overview, "Status", str(bp.getStatus()));
        addRow(overview, "Funding Required",
                bp.getFundingRequired() != null
                        ? String.format("$%,.2f", bp.getFundingRequired()) : "N/A");
        addRow(overview, "Timeline", str(bp.getTimeline()));
        addRow(overview, "Creation Date",
                bp.getCreationDate() != null
                        ? bp.getCreationDate().format(DATE_FMT) : "N/A");
        addRow(overview, "Last Update",
                bp.getLastUpdate() != null
                        ? bp.getLastUpdate().format(DATE_FMT) : "N/A");
        addRow(overview, "Startup ID", String.valueOf(bp.getStartupID()));
        doc.add(overview);

        // ── Scoring ──
        addSection(doc, "Scoring & Assessment");
        PdfPTable scoreT = new PdfPTable(2);
        scoreT.setWidthPercentage(100);
        scoreT.setWidths(new float[]{30, 70});
        scoreT.setSpacingBefore(6);
        addRow(scoreT, "Plan Score", String.format("%.0f / 100", score));
        addRow(scoreT, "Risk Band", InvestmentScorer.band(score));
        doc.add(scoreT);

        // ── Structured sections ──
        addTextSection(doc, "Description", bp.getDescription());
        addTextSection(doc, "Market Analysis", bp.getMarketAnalysis());
        addTextSection(doc, "Value Proposition", bp.getValueProposition());
        addTextSection(doc, "Business Model", bp.getBusinessModel());
        addTextSection(doc, "Marketing Strategy", bp.getMarketingStrategy());
        addTextSection(doc, "Financial Forecast", bp.getFinancialForecast());

        // ── Footer ──
        addFooter(doc);
        doc.close();
    }

    // ─────────────────────────────────────────────────────────
    // Layout helpers
    // ─────────────────────────────────────────────────────────

    private static void addHeader(Document doc, String reportType, String entityName)
            throws DocumentException {
        // Purple bar
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100);
        PdfPCell barCell = new PdfPCell(new Phrase("StartupFlow", BADGE_FONT));
        barCell.setBackgroundColor(BRAND_PURPLE);
        barCell.setPadding(10);
        barCell.setBorder(0);
        barCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        bar.addCell(barCell);
        doc.add(bar);
        doc.add(Chunk.NEWLINE);

        // Title
        Paragraph title = new Paragraph(reportType, TITLE_FONT);
        title.setSpacingAfter(4);
        doc.add(title);

        // Entity name
        if (entityName != null && !entityName.isBlank()) {
            Paragraph sub = new Paragraph(entityName, SUBTITLE_FONT);
            sub.setSpacingAfter(2);
            doc.add(sub);
        }

        // Date
        Paragraph date = new Paragraph(
                "Generated on " + LocalDate.now().format(DATE_FMT), SMALL_FONT);
        date.setSpacingAfter(12);
        doc.add(date);

        // Separator
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setFixedHeight(2);
        sepCell.setBackgroundColor(BRAND_PURPLE_L);
        sepCell.setBorder(0);
        sep.addCell(sepCell);
        doc.add(sep);
        doc.add(Chunk.NEWLINE);
    }

    private static void addSection(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, SECTION_FONT);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        doc.add(p);

        // Thin underline
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(1);
        cell.setBackgroundColor(BRAND_PURPLE_L);
        cell.setBorder(0);
        sep.addCell(cell);
        doc.add(sep);
    }

    private static void addTextSection(Document doc, String label, String content)
            throws DocumentException {
        if (content == null || content.isBlank()) return;

        addSection(doc, label);
        Paragraph p = new Paragraph(content, BODY_FONT);
        p.setSpacingBefore(6);
        p.setSpacingAfter(8);
        p.setLeading(16);
        doc.add(p);
    }

    private static void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(0);
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(ROW_EVEN);
        labelCell.setBorderWidthBottom(0.5f);
        labelCell.setBorderColorBottom(ROW_BORDER);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "—", BODY_FONT));
        valueCell.setBorder(0);
        valueCell.setPadding(6);
        valueCell.setBorderWidthBottom(0.5f);
        valueCell.setBorderColorBottom(ROW_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        PdfPCell fCell = new PdfPCell(new Phrase(
                "StartupFlow Tunisia  •  Confidential  •  " + LocalDate.now().format(DATE_FMT),
                SMALL_FONT));
        fCell.setBorder(0);
        fCell.setBackgroundColor(BRAND_PURPLE_L);
        fCell.setPadding(8);
        fCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        footer.addCell(fCell);
        doc.add(footer);
    }

    private static String str(String s) {
        return (s != null && !s.isBlank()) ? s : "N/A";
    }
}

