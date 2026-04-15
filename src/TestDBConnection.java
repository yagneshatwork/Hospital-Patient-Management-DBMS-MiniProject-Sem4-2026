import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("SUCCESS: Connected to the database!");
                conn.close();
            } else {
                System.out.println("FAILURE: Connection is null.");
            }
        } catch (SQLException e) {
            System.out.println("FAILURE: Exception occurred while connecting.");
            e.printStackTrace();
        }
    }
}
