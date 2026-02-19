package tn.esprit.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.entity.Evaluation;
import tn.esprit.service.EvaluationService;
import tn.esprit.service.ICrud;

public class EvaluationController {

    @FXML private TextField txtFundingApplicationId;
    @FXML private TextField txtScore;
    @FXML private TextField txtDecision;
    @FXML private TextField txtEvaluationComments;
    @FXML private TextField txtEvaluatorId;
    @FXML private TextField txtRiskLevel;
    @FXML private TextField txtFundingCategory;

    @FXML private TextField searchField;

    @FXML private TableView<Evaluation> tableEvaluations;
    @FXML private TableColumn<Evaluation, Integer> colId;
    @FXML private TableColumn<Evaluation, Integer> colFundingApplicationId;
    @FXML private TableColumn<Evaluation, Integer> colScore;
    @FXML private TableColumn<Evaluation, String> colDecision;
    @FXML private TableColumn<Evaluation, String> colEvaluationComments;
    @FXML private TableColumn<Evaluation, Integer> colEvaluatorId;
    @FXML private TableColumn<Evaluation, String> colRiskLevel;
    @FXML private TableColumn<Evaluation, String> colFundingCategory;

    private final ICrud<Evaluation> service = new EvaluationService();
    private ObservableList<Evaluation> masterData;
    private FilteredList<Evaluation> filteredData;

    private Evaluation selectedEvaluation;

    @FXML
    public void initialize() {
        System.out.println("✅ EvaluationController Loaded");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFundingApplicationId.setCellValueFactory(new PropertyValueFactory<>("fundingApplicationId"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colDecision.setCellValueFactory(new PropertyValueFactory<>("decision"));
        colEvaluationComments.setCellValueFactory(new PropertyValueFactory<>("evaluationComments"));
        colEvaluatorId.setCellValueFactory(new PropertyValueFactory<>("evaluatorId"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colFundingCategory.setCellValueFactory(new PropertyValueFactory<>("fundingCategory"));

        limitNumericField(txtFundingApplicationId, 8);
        limitNumericField(txtScore, 8);
        limitNumericField(txtEvaluatorId, 8);

        limitTextField(txtDecision, 8);
        limitTextField(txtRiskLevel, 8);
        limitTextField(txtFundingCategory, 8);

        loadEvaluations();
        setupSearch();
        setupSelection();
    }

    private void loadEvaluations() {
        masterData = FXCollections.observableArrayList(service.getAll());
        filteredData = new FilteredList<>(masterData, b -> true);

        SortedList<Evaluation> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableEvaluations.comparatorProperty());

        tableEvaluations.setItems(sortedData);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filteredData.setPredicate(e -> {
                if (newValue == null || newValue.trim().isEmpty()) return true;

                String keyword = newValue.toLowerCase();

                return String.valueOf(e.getId()).contains(keyword)
                        || String.valueOf(e.getFundingApplicationId()).contains(keyword)
                        || String.valueOf(e.getScore()).contains(keyword)
                        || safeLower(e.getDecision()).contains(keyword)
                        || safeLower(e.getEvaluationComments()).contains(keyword)
                        || String.valueOf(e.getEvaluatorId()).contains(keyword)
                        || safeLower(e.getRiskLevel()).contains(keyword)
                        || safeLower(e.getFundingCategory()).contains(keyword);
            });
        });
    }

    private void setupSelection() {
        tableEvaluations.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedEvaluation = newSel;

            if (newSel != null) {
                txtFundingApplicationId.setText(String.valueOf(newSel.getFundingApplicationId()));
                txtScore.setText(String.valueOf(newSel.getScore()));
                txtDecision.setText(nullToEmpty(newSel.getDecision()));
                txtEvaluationComments.setText(nullToEmpty(newSel.getEvaluationComments()));
                txtEvaluatorId.setText(String.valueOf(newSel.getEvaluatorId()));
                txtRiskLevel.setText(nullToEmpty(newSel.getRiskLevel()));
                txtFundingCategory.setText(nullToEmpty(newSel.getFundingCategory()));
            }
        });
    }

    @FXML
    private void addEvaluation() {
        try {
            Evaluation e = new Evaluation(
                    0,
                    parseIntRequired(txtFundingApplicationId),
                    parseIntRequired(txtScore),
                    txtDecision.getText().trim(),
                    txtEvaluationComments.getText().trim(),
                    parseIntRequired(txtEvaluatorId),
                    txtRiskLevel.getText().trim(),
                    txtFundingCategory.getText().trim()
            );

            service.add(e);
            loadEvaluations();
            clearFields();
            showAlert("Success", "Evaluation added successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", ex.getMessage());
        }
    }

    @FXML
    private void updateEvaluation() {
        if (selectedEvaluation == null) {
            showAlert("Warning", "Select a row first.");
            return;
        }

        try {
            selectedEvaluation.setFundingApplicationId(parseIntRequired(txtFundingApplicationId));
            selectedEvaluation.setScore(parseIntRequired(txtScore));
            selectedEvaluation.setDecision(txtDecision.getText().trim());
            selectedEvaluation.setEvaluationComments(txtEvaluationComments.getText().trim());
            selectedEvaluation.setEvaluatorId(parseIntRequired(txtEvaluatorId));
            selectedEvaluation.setRiskLevel(txtRiskLevel.getText().trim());
            selectedEvaluation.setFundingCategory(txtFundingCategory.getText().trim());

            service.update(selectedEvaluation);
            loadEvaluations();
            clearFields();
            showAlert("Success", "Evaluation updated successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", ex.getMessage());
        }
    }

    @FXML
    private void deleteEvaluation() {
        if (selectedEvaluation == null) {
            showAlert("Warning", "Select a row first.");
            return;
        }

        service.delete(selectedEvaluation.getId());
        loadEvaluations();
        clearFields();
        showAlert("Success", "Evaluation deleted successfully!");
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToApplications(ActionEvent event) {
        switchScene(event, "/gui/application.fxml", "Application CRUD");
    }

    @FXML
    private void goToStats(ActionEvent event) {
        switchScene(event, "/gui/stats.fxml", "Statistics Dashboard");
    }

    private void switchScene(ActionEvent event, String path, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1300, 850));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= VALIDATION =================
    private void limitTextField(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= maxLength ? change : null));
    }

    private void limitNumericField(TextField field, int maxLength) {
        field.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= maxLength) {
                return change;
            }
            return null;
        }));
    }

    private int parseIntRequired(TextField field) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("All numeric fields are required.");
        }
        return Integer.parseInt(field.getText().trim());
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void clearFields() {
        txtFundingApplicationId.clear();
        txtScore.clear();
        txtDecision.clear();
        txtEvaluationComments.clear();
        txtEvaluatorId.clear();
        txtRiskLevel.clear();
        txtFundingCategory.clear();
        selectedEvaluation = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
