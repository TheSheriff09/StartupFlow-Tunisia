package tn.esprit.GUI.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.BusinessPlan;
import tn.esprit.entities.Startup;
import tn.esprit.Services.BusinessPlanService;
import tn.esprit.Services.StartupService;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * AdminHomeController — Dashboard overview page.
 *
 * Populates statistics cards, styled charts (PieChart + BarChart),
 * a recent-activity list, and quick summary insights.
 */
public class AdminHomeController implements Initializable {

    // ── FXML — stat cards ────────────────────────────────────
    @FXML private Label statTotal;
    @FXML private Label statActive;
    @FXML private Label statPending;
    @FXML private Label statBP;

    // ── FXML — quick stats ───────────────────────────────────
    @FXML private Label statAvgKpi;
    @FXML private Label statTotalFunding;
    @FXML private Label statTopSector;
    @FXML private Label statTopStage;

    // ── FXML — charts ────────────────────────────────────────
    @FXML private PieChart                       chartBySector;
    @FXML private BarChart<String, Number>       chartByStage;

    // ── FXML — activity ──────────────────────────────────────
    @FXML private VBox activityList;

    // ── FXML — business plans list ───────────────────────────
    @FXML private VBox bpList;

    // ── Services ─────────────────────────────────────────────
    private final StartupService     startupService = new StartupService();
    private final BusinessPlanService bpService     = new BusinessPlanService();

    // ── Init ─────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Startup>      startups = startupService.list();
        List<BusinessPlan> plans    = bpService.list();

        populateStats(startups, plans);
        populatePieChart(startups);
        populateBarChart(startups);
        populateActivity(startups);
        populateBusinessPlans(plans, startups);
    }

    // ── Stats cards ──────────────────────────────────────────

    private void populateStats(List<Startup> startups, List<BusinessPlan> plans) {
        statTotal.setText(String.valueOf(startups.size()));

        long active = startups.stream()
                .filter(s -> "Active".equalsIgnoreCase(s.getStatus()))
                .count();
        long pending = startups.stream()
                .filter(s -> "Under Review".equalsIgnoreCase(s.getStatus())
                          || "Pending".equalsIgnoreCase(s.getStatus()))
                .count();

        statActive.setText(String.valueOf(active));
        statPending.setText(String.valueOf(pending));
        statBP.setText(String.valueOf(plans.size()));

        // Avg KPI
        OptionalDouble avgKpi = startups.stream()
                .filter(s -> s.getKpiScore() != null)
                .mapToDouble(Startup::getKpiScore)
                .average();
        statAvgKpi.setText(avgKpi.isPresent()
                ? String.format("%.1f", avgKpi.getAsDouble()) : "N/A");

        // Total funding
        double totalFunding = startups.stream()
                .filter(s -> s.getFundingAmount() != null)
                .mapToDouble(Startup::getFundingAmount)
                .sum();
        statTotalFunding.setText(totalFunding > 0
                ? "$" + NumberFormat.getIntegerInstance().format((long) totalFunding)
                : "N/A");

        // Top sector
        startups.stream()
                .filter(s -> s.getSector() != null && !s.getSector().isBlank())
                .collect(Collectors.groupingBy(Startup::getSector, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresentOrElse(
                        e -> statTopSector.setText(e.getKey()),
                        () -> statTopSector.setText("N/A"));

        // Top stage
        startups.stream()
                .filter(s -> s.getStage() != null && !s.getStage().isBlank())
                .collect(Collectors.groupingBy(Startup::getStage, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresentOrElse(
                        e -> statTopStage.setText(e.getKey()),
                        () -> statTopStage.setText("N/A"));
    }

    // ── Pie chart: Startups by Sector ────────────────────────

    private void populatePieChart(List<Startup> startups) {
        Map<String, Long> bySector = startups.stream()
                .filter(s -> s.getSector() != null && !s.getSector().isBlank())
                .collect(Collectors.groupingBy(Startup::getSector, Collectors.counting()));

        List<PieChart.Data> data = bySector.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new PieChart.Data(
                        e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                .collect(Collectors.toList());

        if (data.isEmpty()) data.add(new PieChart.Data("No data", 1));

        chartBySector.setData(FXCollections.observableArrayList(data));
        chartBySector.setTitle("");
        chartBySector.setLegendVisible(true);
        chartBySector.setLabelsVisible(false);
    }

    // ── Bar chart: Startups by Stage ─────────────────────────

    private void populateBarChart(List<Startup> startups) {
        List<String> stageOrder = List.of("Idea", "MVP", "Seed", "Growth", "Mature", "Scaling");

        Map<String, Long> byStage = startups.stream()
                .filter(s -> s.getStage() != null && !s.getStage().isBlank())
                .collect(Collectors.groupingBy(Startup::getStage, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Startups");

        for (String stage : stageOrder) {
            series.getData().add(
                    new XYChart.Data<>(stage, byStage.getOrDefault(stage, 0L)));
        }
        // Any extra stages not in the predefined list
        byStage.keySet().stream()
                .filter(k -> !stageOrder.contains(k))
                .sorted()
                .forEach(k -> series.getData().add(
                        new XYChart.Data<>(k, byStage.get(k))));

        chartByStage.getData().clear();
        chartByStage.getData().add(series);
        chartByStage.setTitle("");
    }

    // ── Recent activity ──────────────────────────────────────

    private void populateActivity(List<Startup> startups) {
        activityList.getChildren().clear();
        int limit = Math.min(startups.size(), 8);
        for (int i = 0; i < limit; i++) {
            activityList.getChildren().add(buildRow(startups.get(i)));
        }
        if (startups.isEmpty()) {
            Label empty = new Label("No recent activity.");
            empty.getStyleClass().add("activity-meta");
            activityList.getChildren().add(empty);
        }
    }

    // ── Business Plans list ──────────────────────────────────

    private void populateBusinessPlans(List<BusinessPlan> plans, List<Startup> startups) {
        bpList.getChildren().clear();

        // Build a fast lookup: startupID → name
        Map<Integer, String> startupNames = startups.stream()
                .collect(Collectors.toMap(
                        Startup::getStartupID,
                        s -> s.getName() != null ? s.getName() : "#" + s.getStartupID(),
                        (a, b) -> a));

        if (plans.isEmpty()) {
            Label empty = new Label("No business plans yet.");
            empty.getStyleClass().add("activity-meta");
            bpList.getChildren().add(empty);
            return;
        }

        for (BusinessPlan bp : plans) {
            bpList.getChildren().add(buildBpRow(bp, startupNames));
        }
    }

    private HBox buildBpRow(BusinessPlan bp, Map<Integer, String> startupNames) {
        // Title
        Label lblTitle = new Label(bp.getTitle() != null ? bp.getTitle() : "Untitled");
        lblTitle.getStyleClass().add("activity-name");
        lblTitle.setPrefWidth(220);
        lblTitle.setMaxWidth(220);
        lblTitle.setWrapText(false);
        lblTitle.setEllipsisString("…");

        // Status badge
        String status = bp.getStatus() != null ? bp.getStatus() : "—";
        Label lblStatus = new Label(status);
        lblStatus.setPrefWidth(110);
        String badgeStyle = switch (status.toLowerCase()) {
            case "approved"     -> "status-badge-approved";
            case "active"       -> "status-badge-active";
            case "under review", "review" -> "status-badge-review";
            case "scaling"      -> "status-badge-scaling";
            case "inactive"     -> "status-badge-inactive";
            default             -> "status-badge-default";
        };
        lblStatus.getStyleClass().addAll("status-badge", badgeStyle);

        // Startup name
        String startupName = startupNames.getOrDefault(bp.getStartupID(), "ID " + bp.getStartupID());
        Label lblStartup = new Label(startupName);
        lblStartup.getStyleClass().add("activity-meta");
        lblStartup.setPrefWidth(165);
        lblStartup.setMaxWidth(165);

        // Funding
        String fundingStr = (bp.getFundingRequired() != null && bp.getFundingRequired() > 0)
                ? "$" + NumberFormat.getIntegerInstance().format(bp.getFundingRequired().longValue())
                : "—";
        Label lblFunding = new Label(fundingStr);
        lblFunding.getStyleClass().add("activity-meta");
        lblFunding.setPrefWidth(155);

        // Creation date
        Label lblDate = new Label(bp.getCreationDate() != null ? bp.getCreationDate().toString() : "—");
        lblDate.getStyleClass().add("activity-meta");
        lblDate.setPrefWidth(110);

        HBox row = new HBox(0, lblTitle, lblStatus, lblStartup, lblFunding, lblDate);
        row.getStyleClass().add("bp-table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(6, 10, 6, 10));
        return row;
    }

    private HBox buildRow(Startup s) {
        // Status dot
        Label dot = new Label();
        dot.getStyleClass().add("activity-dot");
        if ("Active".equalsIgnoreCase(s.getStatus()))
            dot.getStyleClass().add("activity-dot-active");
        else if ("Under Review".equalsIgnoreCase(s.getStatus())
              || "Pending".equalsIgnoreCase(s.getStatus()))
            dot.getStyleClass().add("activity-dot-pending");

        // Name + meta
        Label name = new Label(s.getName() != null ? s.getName() : "Unnamed");
        name.getStyleClass().add("activity-name");

        String sigil = (s.getSector() != null ? s.getSector() : "—")
                + "  ·  "
                + (s.getStage()  != null ? s.getStage()  : "—")
                + "  ·  "
                + (s.getStatus() != null ? s.getStatus() : "—");
        Label meta = new Label(sigil);
        meta.getStyleClass().add("activity-meta");

        VBox info = new VBox(1, name, meta);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Creation date on right
        Label dateLabel = new Label(s.getCreationDate() != null
                ? s.getCreationDate().toString() : "");
        dateLabel.getStyleClass().add("activity-meta");

        HBox row = new HBox(10, dot, info, dateLabel);
        row.getStyleClass().add("activity-item");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}

