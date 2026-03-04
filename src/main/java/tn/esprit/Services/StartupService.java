package tn.esprit.Services;

import tn.esprit.entities.BusinessPlan;
import tn.esprit.entities.Startup;
import tn.esprit.utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StartupService implements ICRUD<Startup> {

    private final Connection cnx;

    public StartupService() {
        this.cnx = MyDB.getInstance().getCnx();
        ensureUserIdColumn("startup");
    }

    private void ensureUserIdColumn(String tableName) {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN user_id INT DEFAULT NULL");
            System.out.println("[Migration] Added user_id column to " + tableName);
        } catch (SQLException e) {
            // Column already exists or table doesn't exist yet, ignore
        }
    }

    // ── Helper: build Startup from current ResultSet row ──────

    private Startup map(ResultSet rs) throws SQLException {
        Date cd = rs.getDate("creationDate");
        Date led = rs.getDate("lastEvaluationDate");

        double kpi = rs.getDouble("KPIscore");
        Double kpiScore = rs.wasNull() ? null : kpi;

        double fa = rs.getDouble("fundingAmount");
        Double fundingAmount = rs.wasNull() ? null : fa;

        int mid = rs.getInt("mentorID");
        Integer mentorID = rs.wasNull() ? null : mid;

        int fid = rs.getInt("founderID");
        Integer founderID = rs.wasNull() ? null : fid;

        int bpid = rs.getInt("businessPlanID");
        Integer businessPlanID = rs.wasNull() ? null : bpid;

        return new Startup(
                rs.getInt("startupID"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("sector"),
                rs.getString("imageURL"),
                cd != null ? cd.toLocalDate() : null,
                kpiScore,
                led != null ? led.toLocalDate() : null,
                rs.getString("stage"),
                rs.getString("status"),
                mentorID,
                fundingAmount,
                rs.getString("incubatorProgram"),
                founderID,
                businessPlanID,
                rs.getInt("user_id"));
    }

    // ── CREATE ────────────────────────────────────────────────

    @Override
    public Startup add(Startup s) {
        String sql = "INSERT INTO startup " +
                "(name, description, sector, imageURL, creationDate, " +
                " KPIscore, lastEvaluationDate, stage, status, " +
                " mentorID, fundingAmount, incubatorProgram, founderID, businessPlanID, user_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getSector());
            ps.setString(4, s.getImageURL());
            ps.setDate(5, s.getCreationDate() != null ? Date.valueOf(s.getCreationDate()) : null);
            if (s.getKpiScore() != null)
                ps.setDouble(6, s.getKpiScore());
            else
                ps.setNull(6, Types.DOUBLE);
            ps.setDate(7, s.getLastEvaluationDate() != null ? Date.valueOf(s.getLastEvaluationDate()) : null);
            ps.setString(8, s.getStage());
            ps.setString(9, s.getStatus());
            if (s.getMentorID() != null)
                ps.setInt(10, s.getMentorID());
            else
                ps.setNull(10, Types.INTEGER);
            if (s.getFundingAmount() != null)
                ps.setDouble(11, s.getFundingAmount());
            else
                ps.setNull(11, Types.DOUBLE);
            ps.setString(12, s.getIncubatorProgram());
            if (s.getFounderID() != null)
                ps.setInt(13, s.getFounderID());
            else
                ps.setNull(13, Types.INTEGER);
            if (s.getBusinessPlanID() != null)
                ps.setInt(14, s.getBusinessPlanID());
            else
                ps.setNull(14, Types.INTEGER);
            ps.setInt(15, s.getUserId());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    s.setStartupID(keys.getInt(1));
            }
            System.out.println("[StartupService] Added startup id=" + s.getStartupID());
        } catch (SQLException e) {
            System.err.println("[StartupService.add] " + e.getMessage());
        }
        return s;
    }

    // ── READ ALL ──────────────────────────────────────────────

    @Override
    public List<Startup> list() {
        List<Startup> result = new ArrayList<>();
        String sql = "SELECT * FROM startup ORDER BY startupID DESC";

        Integer entrepreneurId = null;
        if (tn.esprit.utils.SessionManager.isLoggedIn()) {
            String role = tn.esprit.utils.SessionManager.getRole();
            if ("ENTREPRENEUR".equalsIgnoreCase(role)) {
                entrepreneurId = tn.esprit.utils.SessionManager.getUser().getId();
                sql = "SELECT * FROM startup WHERE user_id = ? ORDER BY startupID DESC";
            }
        }

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (entrepreneurId != null) {
                ps.setInt(1, entrepreneurId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StartupService.list] " + e.getMessage());
        }
        return result;
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Override
    public void update(Startup s) {
        String sql = "UPDATE startup SET " +
                "name=?, description=?, sector=?, imageURL=?, creationDate=?, " +
                "KPIscore=?, lastEvaluationDate=?, stage=?, status=?, " +
                "mentorID=?, fundingAmount=?, incubatorProgram=?, founderID=?, businessPlanID=?, user_id=? " +
                "WHERE startupID=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getSector());
            ps.setString(4, s.getImageURL());
            ps.setDate(5, s.getCreationDate() != null ? Date.valueOf(s.getCreationDate()) : null);
            if (s.getKpiScore() != null)
                ps.setDouble(6, s.getKpiScore());
            else
                ps.setNull(6, Types.DOUBLE);
            ps.setDate(7, s.getLastEvaluationDate() != null ? Date.valueOf(s.getLastEvaluationDate()) : null);
            ps.setString(8, s.getStage());
            ps.setString(9, s.getStatus());
            if (s.getMentorID() != null)
                ps.setInt(10, s.getMentorID());
            else
                ps.setNull(10, Types.INTEGER);
            if (s.getFundingAmount() != null)
                ps.setDouble(11, s.getFundingAmount());
            else
                ps.setNull(11, Types.DOUBLE);
            ps.setString(12, s.getIncubatorProgram());
            if (s.getFounderID() != null)
                ps.setInt(13, s.getFounderID());
            else
                ps.setNull(13, Types.INTEGER);
            if (s.getBusinessPlanID() != null)
                ps.setInt(14, s.getBusinessPlanID());
            else
                ps.setNull(14, Types.INTEGER);
            ps.setInt(15, s.getUserId());
            ps.setInt(16, s.getStartupID());

            ps.executeUpdate();
            System.out.println("[StartupService] Updated startup id=" + s.getStartupID());
        } catch (SQLException e) {
            System.err.println("[StartupService.update] " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    @Override
    public void delete(Startup s) {
        String sql = "DELETE FROM startup WHERE startupID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getStartupID());
            ps.executeUpdate();
            System.out.println("[StartupService] Deleted startup id=" + s.getStartupID());
        } catch (SQLException e) {
            System.err.println("[StartupService.delete] " + e.getMessage());
        }
    }

    // ── GET BY ID ─────────────────────────────────────────────

    public Startup getById(int id) {
        String sql = "SELECT * FROM startup WHERE startupID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("[StartupService.getById] " + e.getMessage());
        }
        return null;
    }

    // ── INVESTMENT SCORING ────────────────────────────────────

    /**
     * Calculates an Investment Score (0–100) for the given startup by
     * fetching all its BusinessPlans and delegating to {@link InvestmentScorer}.
     *
     * @param startupID the PK of the startup
     * @return score in [0.0, 100.0]; 0.0 if the startup has no plans
     */
    public double calculateInvestmentScore(int startupID) {
        BusinessPlanService bpService = new BusinessPlanService();
        List<BusinessPlan> plans = bpService.getByStartup(startupID);
        return InvestmentScorer.calculate(plans);
    }
}
