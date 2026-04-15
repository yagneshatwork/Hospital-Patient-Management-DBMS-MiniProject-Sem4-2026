import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * Provides a static factory method to obtain a MySQL JDBC connection.
 * Change DB_URL, DB_USER, and DB_PASS to match your local MySQL setup.
 */
public class DBConnection {

    // ── Configuration ────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/hospital_db"
                                        + "?useSSL=false&allowPublicKeyRetrieval=true"
                                        + "&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";   // ← Empty password for default XAMPP/WAMP
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a new Connection to hospital_db.
     * Caller is responsible for closing the connection.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC Driver not found.\n"
              + "Make sure mysql-connector-j-*.jar is in the lib/ folder.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
