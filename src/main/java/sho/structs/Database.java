package sho.structs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database implements AutoCloseable {
    private final Connection conn;

    public Database(String dbPath) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS main (key TEXT PRIMARY KEY, value TEXT)");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public void put(String key, String id, String value) {
        String sql =
                "INSERT INTO main (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value ="
                        + " excluded.value";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            key = key + (id == null ? "" : ":" + id);
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to put key: " + key, e);
        }
    }

    public String get(String key, String id) {
        String sql = "SELECT value FROM main WHERE key = ?";
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

    public void delete(String key, String id) {
        String sql = "DELETE FROM main WHERE key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            key = key + (id == null ? "" : ":" + id);
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete key: " + key, e);
        }
    }

    public boolean exists(String key, String id) {
        String sql = "SELECT 1 FROM main WHERE key = ?";
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

    public void clear() {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM main");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear database", e);
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
