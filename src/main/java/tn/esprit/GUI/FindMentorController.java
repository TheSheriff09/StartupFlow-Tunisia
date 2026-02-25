package tn.esprit.GUI;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.MentorRecoRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FindMentorController {

    @FXML private Label lblInfo;
    @FXML private Button btnFind;

    @FXML private TableView<MentorRecoRow> table;

    @FXML private TableColumn<MentorRecoRow, String> colBest;
    @FXML private TableColumn<MentorRecoRow, String> colName;
    @FXML private TableColumn<MentorRecoRow, String> colExpertise;
    @FXML private TableColumn<MentorRecoRow, String> colRating;  // ✅ NEW
    @FXML private TableColumn<MentorRecoRow, String> colRecl;
    @FXML private TableColumn<MentorRecoRow, String> colRisk;

    private final ObservableList<MentorRecoRow> data = FXCollections.observableArrayList();

    // ✅ Use ONE environment only (recommended: project .venv)
    private static final String PYTHON_EXE =
            "C:\\Users\\linaf\\OneDrive\\Bureau\\3A2PiDEV\\.venv\\Scripts\\python.exe";

    // ✅ Script relative to WORKDIR (project root)
    private static final String PY_SCRIPT = "ai\\predict_mentors_ml.py";

    @FXML
    private void initialize() {
        setupTable();
        table.setItems(data);

        if (lblInfo != null) lblInfo.setText("Click Find Mentor to load ML ranking.");
    }

    private void setupTable() {
        colBest.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isBest() ? "BEST ✅" : "")
        );
        colName.setCellValueFactory(cd ->
                new SimpleStringProperty(ns(cd.getValue().getFullName()))
        );
        colExpertise.setCellValueFactory(cd ->
                new SimpleStringProperty(ns(cd.getValue().getExpertise()))
        );

        // ✅ ML predicted probability (0..100)
        colRating.setCellValueFactory(cd ->
                new SimpleStringProperty(String.format("%.2f%%", cd.getValue().getRatingPercent()))
        );

        colRecl.setCellValueFactory(cd ->
                new SimpleStringProperty(String.valueOf(cd.getValue().getReclamations90d()))
        );
        colRisk.setCellValueFactory(cd ->
                new SimpleStringProperty(ns(cd.getValue().getRisk()))
        );
    }

    @FXML
    private void findMentors() {
        try {
            if (lblInfo != null) lblInfo.setText("Running ML prediction...");

            System.out.println("PYTHON_EXE=" + PYTHON_EXE);
            System.out.println("PY_SCRIPT=" + PY_SCRIPT);

            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXE, PY_SCRIPT);

            // ✅ Ensure relative paths work
            pb.directory(new File(System.getProperty("user.dir")));
            System.out.println("WORKDIR=" + pb.directory().getAbsolutePath());

            // ✅ Merge stderr into stdout (so we capture errors)
            pb.redirectErrorStream(true);

            Process p = pb.start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            int exit = p.waitFor();
            String output = sb.toString().trim();

            // Always print raw output for debugging
            System.out.println("PYTHON RAW OUTPUT:\n" + output);
            int start = output.indexOf('[');
            int end = output.lastIndexOf(']');
            if (start == -1 || end == -1 || end < start) {
                lblInfo.setText("Python did not return JSON array:\n" + output);
                return;
            }
            String json = output.substring(start, end + 1);
            if (exit != 0) {
                if (lblInfo != null) lblInfo.setText("Python failed (exit=" + exit + "):\n" + output);
                return;
            }

            if (output.isEmpty()) {
                if (lblInfo != null) lblInfo.setText("Python returned empty output.");
                return;
            }

            // ✅ Parse JSON array
            Gson gson = new Gson();
            Type listType = new TypeToken<List<MentorRecoRow>>() {}.getType();
            List<MentorRecoRow> rows = gson.fromJson(json, listType);
            try {
                rows = gson.fromJson(output, listType);
            } catch (JsonSyntaxException ex) {
                // If python printed warnings or text, Gson fails here
                ex.printStackTrace();
                if (lblInfo != null) lblInfo.setText("Bad JSON from python:\n" + output);
                return;
            }

            data.setAll(rows);
            if (lblInfo != null) lblInfo.setText("Mentors ranked: " + rows.size());

        } catch (Exception ex) {
            ex.printStackTrace();
            if (lblInfo != null) lblInfo.setText("Failed: " + ex.getMessage());
        }
    }

    private String ns(String s) { return (s == null) ? "" : s; }
    @FXML
    private void goBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/EntrepreneurDashboard.fxml")
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) table.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("Back failed: " + e.getMessage());
        }
    }
}