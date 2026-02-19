package tn.esprit.services;

import tn.esprit.entities.Booking;
import tn.esprit.entities.Session;
import tn.esprit.utils.MyDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingService implements ICRUD<Booking> {

    private final Connection cnx;

    public BookingService() {
        cnx = MyDB.getInstance().getCnx();
    }

    @Override
    public Booking add(Booking b) {
        if (b.getTopic() == null || b.getTopic().trim().isEmpty())
            throw new IllegalArgumentException("Topic is required.");
        if (b.getRequestedDate() == null)
            throw new IllegalArgumentException("Date is required.");
        if (b.getRequestedDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Date cannot be in the past.");
        if (b.getRequestedTime() == null)
            throw new IllegalArgumentException("Time is required.");
        if (b.getMentorID() <= 0)
            throw new IllegalArgumentException("Mentor ID is required.");
        if (b.getEntrepreneurID() <= 0)
            throw new IllegalArgumentException("Entrepreneur ID is required.");

        String sql = "INSERT INTO booking (entrepreneurID, mentorID, startupID, requestedDate, requestedTime, topic, status) VALUES (?,?,?,?,?,?,'pending')";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getEntrepreneurID());
            ps.setInt(2, b.getMentorID());
            ps.setInt(3, b.getStartupID());
            ps.setDate(4, Date.valueOf(b.getRequestedDate()));
            ps.setTime(5, Time.valueOf(b.getRequestedTime()));
            ps.setString(6, b.getTopic().trim());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) b.setBookingID(rs.getInt(1));
            return b;
        } catch (SQLException e) {
            throw new RuntimeException("Error adding booking: " + e.getMessage());
        }
    }

    @Override
    public List<Booking> list() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM booking ORDER BY creationDate DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error listing bookings: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void update(Booking b) {
        if (b.getTopic() == null || b.getTopic().trim().isEmpty())
            throw new IllegalArgumentException("Topic is required.");
        if (b.getRequestedDate() == null)
            throw new IllegalArgumentException("Date is required.");

        String sql = "UPDATE booking SET entrepreneurID=?, mentorID=?, startupID=?, requestedDate=?, requestedTime=?, topic=?, status=? WHERE bookingID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, b.getEntrepreneurID());
            ps.setInt(2, b.getMentorID());
            ps.setInt(3, b.getStartupID());
            ps.setDate(4, Date.valueOf(b.getRequestedDate()));
            ps.setTime(5, Time.valueOf(b.getRequestedTime()));
            ps.setString(6, b.getTopic().trim());
            ps.setString(7, b.getStatus());
            ps.setInt(8, b.getBookingID());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating booking: " + e.getMessage());
        }
    }

    @Override
    public void delete(Booking b) {
        if ("approved".equalsIgnoreCase(b.getStatus()))
            throw new IllegalStateException("Cannot delete an approved booking.");
        String sql = "DELETE FROM booking WHERE bookingID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, b.getBookingID());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting booking: " + e.getMessage());
        }
    }

    public Session approveBooking(Booking b) {
        if (!"pending".equalsIgnoreCase(b.getStatus()))
            throw new IllegalStateException("Only pending bookings can be approved.");

        String sql = "UPDATE booking SET status='approved' WHERE bookingID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, b.getBookingID());
            ps.executeUpdate();
            b.setStatus("approved");
        } catch (SQLException e) {
            throw new RuntimeException("Error approving booking: " + e.getMessage());
        }

        Session session = new Session(
                b.getMentorID(), b.getEntrepreneurID(), b.getStartupID(),
                b.getRequestedDate(), "online",
                "Auto-created from booking #" + b.getBookingID()
        );
        SessionService sessionService = new SessionService();
        return sessionService.add(session);
    }

    public void rejectBooking(Booking b) {
        if (!"pending".equalsIgnoreCase(b.getStatus()))
            throw new IllegalStateException("Only pending bookings can be rejected.");
        String sql = "UPDATE booking SET status='rejected' WHERE bookingID=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, b.getBookingID());
            ps.executeUpdate();
            b.setStatus("rejected");
        } catch (SQLException e) {
            throw new RuntimeException("Error rejecting booking: " + e.getMessage());
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("bookingID"),
                rs.getInt("entrepreneurID"),
                rs.getInt("mentorID"),
                rs.getInt("startupID"),
                rs.getDate("requestedDate").toLocalDate(),
                rs.getTime("requestedTime").toLocalTime(),
                rs.getString("topic"),
                rs.getString("status"),
                rs.getTimestamp("creationDate")
        );
    }
}
