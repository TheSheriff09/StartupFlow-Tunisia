package tn.esprit.service;

import tn.esprit.utils.MyDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatsService {

    private final Connection conx;

    public StatsService() {
        conx = MyDB.getInstance().getConx();
    }

    public int getApplicationsCount() {
        String sql = "SELECT COUNT(*) FROM fundingapplication";
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("StatsService.getApplicationsCount error: " + e.getMessage());
        }
        return 0;
    }

    public int getEvaluationsCount() {
        String sql = "SELECT COUNT(*) FROM fundingevaluation";
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("StatsService.getEvaluationsCount error: " + e.getMessage());
        }
        return 0;
    }

    public double getAverageScore() {
        String sql = "SELECT AVG(score) FROM fundingevaluation";
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("StatsService.getAverageScore error: " + e.getMessage());
        }
        return 0.0;
    }

    public double getTotalFundingAmount() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM fundingapplication";
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("StatsService.getTotalFundingAmount error: " + e.getMessage());
        }
        return 0.0;
    }

    public Map<String, Integer> getApplicationsByStatus() {
        String sql = "SELECT status, COUNT(*) as c FROM fundingapplication GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("c");
                map.put(status == null || status.trim().isEmpty() ? "Unknown" : status, count);
            }

        } catch (SQLException e) {
            System.out.println("StatsService.getApplicationsByStatus error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getEvaluationsByDecision() {
        String sql = "SELECT decision, COUNT(*) as c FROM fundingevaluation GROUP BY decision";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String decision = rs.getString("decision");
                int count = rs.getInt("c");
                map.put(decision == null || decision.trim().isEmpty() ? "Unknown" : decision, count);
            }

        } catch (SQLException e) {
            System.out.println("StatsService.getEvaluationsByDecision error: " + e.getMessage());
        }
        return map;
    }
}
