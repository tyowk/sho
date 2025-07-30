package sho.structs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database implements AutoCloseable {
    private final Connection conn;
    private final String[] tables;
    private final String path;

    public Database(String dbPath, String[] tables) {
        if (dbPath == null || dbPath.isBlank()) {
            throw new IllegalArgumentException("Database path is missing or empty!");
        }

        if (tables == null || tables.length == 0) {
            throw new IllegalArgumentException("Database tables array is missing or empty!");
        }

        this.path = dbPath;
        this.tables = tables;

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement stmt = conn.createStatement()) {
                for (String table : tables) {
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS "
                                    + table
                                    + " (key TEXT PRIMARY KEY, value LONGTEXT)");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public void put(String table, String key, String id, String value) {
        String sql =
                "INSERT INTO "
                        + table
                        + " (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            key = key + (id == null ? "" : ":" + id);
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to put key: " + key, e);
        }
    }

    public String get(String table, String key, String id) {
        String sql = "SELECT value FROM " + table + " WHERE key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            key = key + (id == null ? "" : ":" + id);
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get key: " + key, e);
        }
    }

    public void delete(String table, String key, String id) {
        String sql = "DELETE FROM " + table + " WHERE key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            key = key + (id == null ? "" : ":" + id);
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete key: " + key, e);
        }
    }

    public boolean exists(String table, String key, String id) {
        String sql = "SELECT 1 FROM " + table + " WHERE key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            key = key + (id == null ? "" : ":" + id);
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check key: " + key, e);
        }
    }

    public void clear(String table) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + table);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear database", e);
        }
    }

    public void drop(String table) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + table);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to drop table", e);
        }
    }

    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database", e);
        }
    }
}
