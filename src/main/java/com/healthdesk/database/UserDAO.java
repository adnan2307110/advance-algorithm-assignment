package com.healthdesk.database;

import com.healthdesk.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities.
 */
public class UserDAO {

    public User authenticate(String username, String password, String role) {
        String sql = "SELECT * FROM User WHERE LOWER(username) = LOWER(?) AND password = ? AND LOWER(role) = LOWER(?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());
            pstmt.setString(3, role.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Auth error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM User ORDER BY id ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching users: " + e.getMessage());
        }
        return list;
    }

    public boolean addUser(String username, String password, String role, String fullName) {
        String sql = "INSERT INTO User (username, password, role, full_name) VALUES (?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, fullName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Add user error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM User WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Delete user error: " + e.getMessage());
            return false;
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String pass = rs.getString("password");
        String role = rs.getString("role");
        String fullName = rs.getString("full_name");

        switch (role.toLowerCase()) {
            case "admin":
                return new Admin(id, username, pass, fullName);
            case "doctor":
                // Find matching doctor record if possible, or fallback to id
                int docId = findDoctorIdByName(fullName, id);
                return new DoctorUser(id, username, pass, fullName, docId);
            case "receptionist":
                return new Receptionist(id, username, pass, fullName);
            case "lab technician":
            case "labtechnician":
                return new LabTechnician(id, username, pass, fullName);
            default:
                return new Admin(id, username, pass, fullName);
        }
    }

    private int findDoctorIdByName(String fullName, int fallback) {
        String sql = "SELECT id FROM Doctor WHERE LOWER(name) LIKE LOWER(?) LIMIT 1;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + fullName.replace("Dr. ", "").trim() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException ignored) {}
        return 1;
    }
}
