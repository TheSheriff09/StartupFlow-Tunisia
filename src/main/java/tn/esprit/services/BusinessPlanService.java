package tn.esprit.services;

import tn.esprit.entities.BusinessPlan;
import tn.esprit.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusinessPlanService implements ICRUD<BusinessPlan> {

    private final Connection cnx;

    public BusinessPlanService() {
        this.cnx = DatabaseConnection.getInstance().getConnection();
    }

    // ── Helper: map ResultSet row → BusinessPlan ──────────────

    private BusinessPlan map(ResultSet rs) throws SQLException {
        Date cd = rs.getDate("creationDate");
        Date lu = rs.getDate("lastUpdate");

        double fr = rs.getDouble("fundingRequired");
        Double fundingRequired = rs.wasNull() ? null : fr;

        return new BusinessPlan(
                rs.getInt("businessPlanID"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("marketAnalysis"),
                rs.getString("valueProposition"),
                rs.getString("businessModel"),
                rs.getString("marketingStrategy"),
                rs.getString("financialForecast"),
                fundingRequired,
                rs.getString("timeline"),
                rs.getString("status"),
                cd != null ? cd.toLocalDate() : null,
                lu != null ? lu.toLocalDate() : null,
                rs.getInt("startupID")
        );
    }

    // ── CREATE ────────────────────────────────────────────────

    @Override
    public BusinessPlan add(BusinessPlan bp) {
        String sql = "INSERT INTO businessplan " +
                "(title, description, marketAnalysis, valueProposition, businessModel, " +
                " marketingStrategy, financialForecast, fundingRequired, timeline, " +
                " status, creationDate, lastUpdate, startupID) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  bp.getTitle());
            ps.setString(2,  bp.getDescription());
            ps.setString(3,  bp.getMarketAnalysis());
            ps.setString(4,  bp.getValueProposition());
            ps.setString(5,  bp.getBusinessModel());
            ps.setString(6,  bp.getMarketingStrategy());
            ps.setString(7,  bp.getFinancialForecast());
            if (bp.getFundingRequired() != null) ps.setDouble(8, bp.getFundingRequired());
            else ps.setNull(8, Types.DOUBLE);
            ps.setString(9,  bp.getTimeline());
            ps.setString(10, bp.getStatus());
            ps.setDate(11,   bp.getCreationDate() != null ? Date.valueOf(bp.getCreationDate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setDate(12,   bp.getLastUpdate() != null ? Date.valueOf(bp.getLastUpdate()) : Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(13,    bp.getStartupID());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) bp.setBusinessPlanID(keys.getInt(1));
            }
            System.out.println("[BusinessPlanService] Added plan id=" + bp.getBusinessPlanID());
        } catch (SQLException e) {
            System.err.println("[BusinessPlanService.add] " + e.getMessage());
        }
        return bp;
    }

    // ── READ ALL ──────────────────────────────────────────────

    @Override
    public List<BusinessPlan> list() {
        return fetch("SELECT * FROM businessplan ORDER BY businessPlanID DESC", null);
    }

    // ── READ BY STARTUP (key method for navigation) ───────────

    public List<BusinessPlan> getByStartup(int startupID) {
        return fetch("SELECT * FROM businessplan WHERE startupID=? ORDER BY businessPlanID DESC", startupID);
    }

    private List<BusinessPlan> fetch(String sql, Integer startupID) {
        List<BusinessPlan> result = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (startupID != null) ps.setInt(1, startupID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BusinessPlanService.fetch] " + e.getMessage());
        }
        return result;
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Override
    public void update(BusinessPlan bp) {
        String sql = "UPDATE businessplan SET " +
                "title=?, description=?, marketAnalysis=?, valueProposition=?, businessModel=?, " +
                "marketingStrategy=?, financialForecast=?, fundingRequired=?, timeline=?, " +
                "status=?, lastUpdate=?, startupID=? " +
                "WHERE businessPlanID=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1,  bp.getTitle());
            ps.setString(2,  bp.getDescription());
            ps.setString(3,  bp.getMarketAnalysis());
            ps.setString(4,  bp.getValueProposition());
            ps.setString(5,  bp.getBusinessModel());
            ps.setString(6,  bp.getMarketingStrategy());
            ps.setString(7,  bp.getFinancialForecast());
            if (bp.getFundingRequired() != null) ps.setDouble(8, bp.getFundingRequired());
            else ps.setNull(8, Types.DOUBLE);
            ps.setString(9,  bp.getTimeline());
            ps.setString(10, bp.getStatus());
            ps.setDate(11,   Date.valueOf(java.time.LocalDate.now())); // auto-set lastUpdate
            ps.setInt(12,    bp.getStartupID());
            ps.setInt(13,    bp.getBusinessPlanID());

            ps.executeUpdate();
            System.out.println("[BusinessPlanService] Updated plan id=" + bp.getBusinessPlanID());
        } catch (SQLException e) {
            System.err.println("[BusinessPlanService.update] " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    @Override
    public void delete(BusinessPlan bp) {
        String sql = "DELETE FROM businessplan WHERE businessPlanID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, bp.getBusinessPlanID());
            ps.executeUpdate();
            System.out.println("[BusinessPlanService] Deleted plan id=" + bp.getBusinessPlanID());
        } catch (SQLException e) {
            System.err.println("[BusinessPlanService.delete] " + e.getMessage());
        }
    }

    // ── GET BY ID ─────────────────────────────────────────────

    @Override
    public BusinessPlan getById(int id) {
        String sql = "SELECT * FROM businessplan WHERE businessPlanID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("[BusinessPlanService.getById] " + e.getMessage());
        }
        return null;
    }
}
