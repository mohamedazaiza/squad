package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 */
public class DatabaseConnector {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(Constants.DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("CRITICAL: Failed to connect to the database: " + Constants.DB_FILE_NAME + ". Error: " + e.getMessage());
            throw e; 
        }
        return connection;
    }

    /**
     * Closes the current database connection if it is open.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            } finally {
                connection = null; 
            }
        }
    }

}