package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thread-safe singleton database connection with automatic reconnect.
 *
 * Calling {@link #getConnection()} always returns a live {@link Connection}:
 * if the underlying socket was dropped (e.g. MySQL wait_timeout) the method
 * transparently re-opens it before returning.
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private static final String URL      = "jdbc:mysql://localhost:3306/startupflow";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        openConnection();
    }

    /** Double-checked locking — safe for concurrent JavaFX + background threads. */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns a valid {@link Connection}, reconnecting silently if needed.
     *
     * @return live Connection, or {@code null} if the DB is genuinely unreachable.
     */
    public Connection getConnection() {
        try {
            if (connection == null || !connection.isValid(2)) {
                System.out.println("[DB] Connection invalid — reconnecting…");
                openConnection();
            }
        } catch (SQLException e) {
            System.err.println("[DB] isValid() check failed: " + e.getMessage());
            openConnection();
        }
        return connection;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void openConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to 'startupflow' successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] Connection error: " + e.getMessage());
            connection = null;
        }
    }
}
