package tn.esprit.utils;

public class NavContext {
    // The FXML we should go back to (set before opening the page)
    public static String backFxml = "/EntrepreneurDashboard.fxml";

    public static void setBack(String fxml) {
        if (fxml != null && !fxml.trim().isEmpty()) backFxml = fxml;
    }
}