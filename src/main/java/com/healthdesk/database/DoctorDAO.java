package com.healthdesk.database;

import com.healthdesk.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Doctor entity.
 */
public class DoctorDAO {

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM Doctor ORDER BY id ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapDoctor(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Error fetching doctors: " + e.getMessage());
        }
        return list;
    }

    public Doctor getDoctorById(int id) {
        String sql = "SELECT * FROM Doctor WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapDoctor(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Error fetching doctor by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Doctor> searchDoctors(String query) {
        List<Doctor> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllDoctors();
        }
        String sql = "SELECT * FROM Doctor WHERE name LIKE ? OR specialization LIKE ? OR room LIKE ? ORDER BY id ASC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcard = "%" + query.trim() + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            pstmt.setString(3, wildcard);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDoctor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Search error: " + e.getMessage());
        }
        return list;
    }

    public boolean addDoctor(Doctor doctor) {
        String sql = "INSERT INTO Doctor (name, specialization, phone, room, available_time) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getPhone());
            pstmt.setString(4, doctor.getRoom());
            pstmt.setString(5, doctor.getAvailableTime());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        doctor.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Add doctor error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateDoctor(Doctor doctor) {
        String sql = "UPDATE Doctor SET name = ?, specialization = ?, phone = ?, room = ?, available_time = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getName());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setString(3, doctor.getPhone());
            pstmt.setString(4, doctor.getRoom());
            pstmt.setString(5, doctor.getAvailableTime());
            pstmt.setInt(6, doctor.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Update doctor error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDoctor(int id) {
        String sql = "DELETE FROM Doctor WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Delete doctor error: " + e.getMessage());
            return false;
        }
    }

    public int getDoctorCount() {
        String sql = "SELECT COUNT(*) FROM Doctor;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DoctorDAO] Count error: " + e.getMessage());
        }
        return 0;
    }

    private Doctor mapDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("specialization"),
                rs.getString("phone"),
                rs.getString("room"),
                rs.getString("available_time")
        );
    }
}
