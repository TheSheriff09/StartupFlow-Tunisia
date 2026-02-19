package tn.esprit.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.service.StatsService;

import java.util.Map;

public class StatsController {

    @FXML private PieChart appStatusChart;
    @FXML private PieChart evalDecisionChart;

    @FXML private Label lblTotalApps;
    @FXML private Label lblTotalEvals;
    @FXML private Label lblAvgScore;
    @FXML private Label lblTotalAmount;

    private final StatsService statsService = new StatsService();

    @FXML
    public void initialize() {
        loadStats();
    }

    @FXML
    private void refresh() {
        loadStats();
    }

    private void loadStats() {
        // Summary
        int totalApps = statsService.getApplicationsCount();
        int totalEvals = statsService.getEvaluationsCount();
        double avgScore = statsService.getAverageScore();
        double totalAmount = statsService.getTotalFundingAmount();

        lblTotalApps.setText("Total Applications: " + totalApps);
        lblTotalEvals.setText("Total Evaluations: " + totalEvals);
        lblAvgScore.setText(String.format("Average Score: %.2f", avgScore));
        lblTotalAmount.setText(String.format("Total Funding Amount: %.2f", totalAmount));

        // Charts
        appStatusChart.setTitle("Applications by Status");
        evalDecisionChart.setTitle("Evaluations by Decision");

        appStatusChart.setData(mapToPieData(statsService.getApplicationsByStatus()));
        evalDecisionChart.setData(mapToPieData(statsService.getEvaluationsByDecision()));
    }

    private ObservableList<PieChart.Data> mapToPieData(Map<String, Integer> map) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            data.add(new PieChart.Data(e.getKey(), e.getValue()));
        }
        return data;
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToApplications(ActionEvent event) {
        switchScene(event, "/gui/application.fxml", "Application CRUD");
    }

    @FXML
    private void goToEvaluation(ActionEvent event) {
        switchScene(event, "/gui/evaluation.fxml", "Evaluation CRUD");
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1300, 850));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot open page: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
