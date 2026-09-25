package com.healthdesk.database;

import com.healthdesk.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Patient entity.
 */
public class PatientDAO {

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient ORDER BY id ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPatient(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Error fetching patients: " + e.getMessage());
        }
        return list;
    }

    public Patient getPatientById(int id) {
        String sql = "SELECT * FROM Patient WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapPatient(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Error fetching patient by ID: " + e.getMessage());
        }
        return null;
    }

    public List<Patient> searchPatients(String query) {
        List<Patient> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllPatients();
        }
        String sql = "SELECT * FROM Patient WHERE name LIKE ? OR phone LIKE ? OR blood_group LIKE ? OR address LIKE ? ORDER BY id ASC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcard = "%" + query.trim() + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            pstmt.setString(3, wildcard);
            pstmt.setString(4, wildcard);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPatient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Search error: " + e.getMessage());
        }
        return list;
    }

    public boolean addPatient(Patient patient) {
        String sql = "INSERT INTO Patient (name, age, gender, blood_group, phone, address, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getBloodGroup());
            pstmt.setString(5, patient.getPhone());
            pstmt.setString(6, patient.getAddress());
            pstmt.setString(7, patient.getEmergencyContact());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        patient.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Add patient error: " + e.getMessage());
        }
        return false;
    }

    public boolean updatePatient(Patient patient) {
        String sql = "UPDATE Patient SET name = ?, age = ?, gender = ?, blood_group = ?, phone = ?, address = ?, emergency_contact = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getBloodGroup());
            pstmt.setString(5, patient.getPhone());
            pstmt.setString(6, patient.getAddress());
            pstmt.setString(7, patient.getEmergencyContact());
            pstmt.setInt(8, patient.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Update patient error: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePatient(int id) {
        String sql = "DELETE FROM Patient WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Delete patient error: " + e.getMessage());
            return false;
        }
    }

    public int getPatientCount() {
        String sql = "SELECT COUNT(*) FROM Patient;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[PatientDAO] Count error: " + e.getMessage());
        }
        return 0;
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        return new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("blood_group"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("emergency_contact")
        );
    }
}
