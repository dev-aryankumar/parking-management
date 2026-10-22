package com.parking.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection manager.
 *
 * Connection settings can be overridden with environment variables:
 *   MYSQL_PASSWORD  (required when the MySQL root account has a password)
 *   PARKING_DB_USER (default: root)
 *   PARKING_DB_URL  (default: jdbc:mysql://localhost:3306/smart_parking?...)
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";

    private static Connection connection;

    private DBConnection() {
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = env("PARKING_DB_URL", DEFAULT_URL);
                String user = env("PARKING_DB_USER", DEFAULT_USER);
                String password = env("MYSQL_PASSWORD", "");
                connection = DriverManager.getConnection(url, user, password);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL JDBC driver (mysql-connector-j) was not found on the classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Nothing useful to do while shutting down.
            }
            connection = null;
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? fallback : value;
    }
}
