package com.carrental.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hands out JDBC connections built from config.properties
 * (copy config.properties.example -> config.properties and fill in your
 * local MySQL credentials; config.properties is gitignored on purpose).
 */
public final class DatabaseConnection {

    private static final Properties CONFIG = loadConfig();

    private DatabaseConnection() { }

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "config.properties not found on the classpath. Copy " +
                    "config.properties.example to src/main/resources/config.properties " +
                    "and fill in your MySQL credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        String url = CONFIG.getProperty("db.url");
        String user = CONFIG.getProperty("db.user");
        String password = CONFIG.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
