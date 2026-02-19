package tn.esprit.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entity.Application;
import tn.esprit.service.ApplicationService;
import tn.esprit.service.ICrud;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import java.io.File;

public class ApplicationController {

    @FXML private TextField txtEntrepreneurId;
    @FXML private TextField txtAmount;
    @FXML private TextField txtStatus;
    @FXML private TextField txtSubmissionDate;
    @FXML private TextField txtApplicationReason;
    @FXML private TextField txtProjectId;
    @FXML private TextField txtPaymentSchedule;
    @FXML private TextField txtAttachment;

    @FXML private TextField searchField;

    @FXML private TableView<Application> tableApplications;
    @FXML private TableColumn<Application, Integer> colId;
    @FXML private TableColumn<Application, Integer> colEntrepreneurId;
    @FXML private TableColumn<Application, Float> colAmount;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TableColumn<Application, String> colSubmissionDate;
    @FXML private TableColumn<Application, String> colApplicationReason;
    @FXML private TableColumn<Application, Integer> colProjectId;
    @FXML private TableColumn<Application, String> colPaymentSchedule;
    @FXML private TableColumn<Application, String> colAttachment;

    private ICrud<Application> service = new ApplicationService();
    private ObservableList<Application> masterData;
    private FilteredList<Application> filteredData;

    private Application selectedApplication;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEntrepreneurId.setCellValueFactory(new PropertyValueFactory<>("entrepreneurId"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colSubmissionDate.setCellValueFactory(new PropertyValueFactory<>("submissionDate"));
        colApplicationReason.setCellValueFactory(new PropertyValueFactory<>("applicationReason"));
        colProjectId.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        colPaymentSchedule.setCellValueFactory(new PropertyValueFactory<>("paymentSchedule"));
        colAttachment.setCellValueFactory(new PropertyValueFactory<>("attachment"));

        loadApplications();
        setupSearch();
        setupSelection();
    }

    private void loadApplications() {
        masterData = FXCollections.observableArrayList(service.getAll());
        filteredData = new FilteredList<>(masterData, b -> true);

        SortedList<Application> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableApplications.comparatorProperty());

        tableApplications.setItems(sortedData);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(app -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String keyword = newValue.toLowerCase();

                return String.valueOf(app.getId()).contains(keyword)
                        || String.valueOf(app.getEntrepreneurId()).contains(keyword)
                        || String.valueOf(app.getAmount()).contains(keyword)
                        || app.getStatus().toLowerCase().contains(keyword)
                        || app.getSubmissionDate().toLowerCase().contains(keyword)
                        || app.getApplicationReason().toLowerCase().contains(keyword)
                        || String.valueOf(app.getProjectId()).contains(keyword)
                        || app.getPaymentSchedule().toLowerCase().contains(keyword)
                        || app.getAttachment().toLowerCase().contains(keyword);
            });
        });
    }

    private void setupSelection() {
        tableApplications.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {

                    selectedApplication = newSelection;

                    if (newSelection != null) {
                        txtEntrepreneurId.setText(String.valueOf(newSelection.getEntrepreneurId()));
                        txtAmount.setText(String.valueOf(newSelection.getAmount()));
                        txtStatus.setText(newSelection.getStatus());
                        txtSubmissionDate.setText(newSelection.getSubmissionDate());
                        txtApplicationReason.setText(newSelection.getApplicationReason());
                        txtProjectId.setText(String.valueOf(newSelection.getProjectId()));
                        txtPaymentSchedule.setText(newSelection.getPaymentSchedule());
                        txtAttachment.setText(newSelection.getAttachment());
                    }
                }
        );
    }

    @FXML
    private void addApplication() {
        try {
            Application app = new Application(
                    0,
                    Integer.parseInt(txtEntrepreneurId.getText()),
                    Float.parseFloat(txtAmount.getText()),
                    txtStatus.getText(),
                    txtSubmissionDate.getText(),
                    txtApplicationReason.getText(),
                    Integer.parseInt(txtProjectId.getText()),
                    txtPaymentSchedule.getText(),
                    txtAttachment.getText()
            );

            service.add(app);
            loadApplications();
            clearFields();

        } catch (Exception e) {
            showAlert("Error", "Invalid input.");
        }
    }

    @FXML
    private void updateApplication() {
        if (selectedApplication == null) {
            showAlert("Warning", "Select a row first.");
            return;
        }

        try {
            selectedApplication.setEntrepreneurId(Integer.parseInt(txtEntrepreneurId.getText()));
            selectedApplication.setAmount(Float.parseFloat(txtAmount.getText()));
            selectedApplication.setStatus(txtStatus.getText());
            selectedApplication.setSubmissionDate(txtSubmissionDate.getText());
            selectedApplication.setApplicationReason(txtApplicationReason.getText());
            selectedApplication.setProjectId(Integer.parseInt(txtProjectId.getText()));
            selectedApplication.setPaymentSchedule(txtPaymentSchedule.getText());
            selectedApplication.setAttachment(txtAttachment.getText());

            service.update(selectedApplication);

            loadApplications();
            clearFields();

        } catch (Exception e) {
            showAlert("Error", "Invalid input.");
        }
    }

    @FXML
    private void deleteApplication() {
        if (selectedApplication == null) {
            showAlert("Warning", "Select a row first.");
            return;
        }

        service.delete(selectedApplication.getId());
        loadApplications();
        clearFields();
    }

    private void clearFields() {
        txtEntrepreneurId.clear();
        txtAmount.clear();
        txtStatus.clear();
        txtSubmissionDate.clear();
        txtApplicationReason.clear();
        txtProjectId.clear();
        txtPaymentSchedule.clear();
        txtAttachment.clear();
        selectedApplication = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToEvaluation(ActionEvent event) {
        switchScene(event, "/gui/evaluation.fxml", "Evaluation CRUD");
    }

    @FXML
    private void goToStats(ActionEvent event) {
        switchScene(event, "/gui/stats.fxml", "Statistics Dashboard");
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

    @FXML
    private void browseAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(txtAttachment.getScene().getWindow());

        if (selectedFile != null) {
            txtAttachment.setText(selectedFile.getAbsolutePath());
        }
    }
}
