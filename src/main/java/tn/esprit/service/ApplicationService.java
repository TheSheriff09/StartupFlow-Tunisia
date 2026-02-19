package tn.esprit.service;

import tn.esprit.entity.Application;
import tn.esprit.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationService implements ICrud<Application> {

    private Connection conx;

    public ApplicationService() {
        conx = MyDB.getInstance().getConx();
    }

    @Override
    public void add(Application app) {
        String query = "INSERT INTO fundingapplication (entrepreneurId, amount, status, submissionDate, applicationReason, projectId, paymentSchedule, attachment) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, app.getEntrepreneurId());
            ps.setFloat(2, app.getAmount());
            ps.setString(3, app.getStatus());
            ps.setString(4, app.getSubmissionDate());
            ps.setString(5, app.getApplicationReason());
            ps.setInt(6, app.getProjectId());
            ps.setString(7, app.getPaymentSchedule());
            ps.setString(8, app.getAttachment());
            ps.executeUpdate();
            System.out.println("Application added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding application: " + e.getMessage());
        }
    }

    @Override
    public List<Application> getAll() {
        List<Application> list = new ArrayList<>();
        String query = "SELECT * FROM fundingapplication";
        try (Statement st = conx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Application app = new Application(
                        rs.getInt("id"),
                        rs.getInt("entrepreneurId"),
                        rs.getFloat("amount"),
                        rs.getString("status"),
                        rs.getString("submissionDate"),
                        rs.getString("applicationReason"),
                        rs.getInt("projectId"),
                        rs.getString("paymentSchedule"),
                        rs.getString("attachment")
                );
                list.add(app);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching applications: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Application app) {
        String query = "UPDATE fundingapplication SET entrepreneurId=?, amount=?, status=?, submissionDate=?, applicationReason=?, projectId=?, paymentSchedule=?, attachment=? WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, app.getEntrepreneurId());
            ps.setFloat(2, app.getAmount());
            ps.setString(3, app.getStatus());
            ps.setString(4, app.getSubmissionDate());
            ps.setString(5, app.getApplicationReason());
            ps.setInt(6, app.getProjectId());
            ps.setString(7, app.getPaymentSchedule());
            ps.setString(8, app.getAttachment());
            ps.setInt(9, app.getId());
            ps.executeUpdate();
            System.out.println("Application updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating application: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM fundingapplication WHERE id=?";
        try (PreparedStatement ps = conx.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Application deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting application: " + e.getMessage());
        }
    }
}
