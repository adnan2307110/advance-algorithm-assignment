package com.healthdesk.database;

import com.healthdesk.model.Prescription;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Prescription entity.
 */
public class PrescriptionDAO {

    public List<Prescription> getAllPrescriptions() {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT pr.*, p.name AS patient_name, d.name AS doctor_name " +
                     "FROM Prescription pr " +
                     "JOIN Patient p ON pr.patient_id = p.id " +
                     "JOIN Doctor d ON pr.doctor_id = d.id " +
                     "ORDER BY pr.id DESC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPrescription(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PrescriptionDAO] Error fetching prescriptions: " + e.getMessage());
        }
        return list;
    }

    public List<Prescription> getPrescriptionsByPatient(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT pr.*, p.name AS patient_name, d.name AS doctor_name " +
                     "FROM Prescription pr " +
                     "JOIN Patient p ON pr.patient_id = p.id " +
                     "JOIN Doctor d ON pr.doctor_id = d.id " +
                     "WHERE pr.patient_id = ? " +
                     "ORDER BY pr.id DESC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPrescription(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PrescriptionDAO] Error fetching by patient: " + e.getMessage());
        }
        return list;
    }

    public boolean addPrescription(Prescription pr) {
        String sql = "INSERT INTO Prescription (patient_id, doctor_id, medicine, dosage, duration, instructions, date) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, pr.getPatientId());
            pstmt.setInt(2, pr.getDoctorId());
            pstmt.setString(3, pr.getMedicine());
            pstmt.setString(4, pr.getDosage());
            pstmt.setString(5, pr.getDuration());
            pstmt.setString(6, pr.getInstructions());
            pstmt.setString(7, pr.getDate());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        pr.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PrescriptionDAO] Add error: " + e.getMessage());
        }
        return false;
    }

    public boolean deletePrescription(int id) {
        String sql = "DELETE FROM Prescription WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PrescriptionDAO] Delete error: " + e.getMessage());
            return false;
        }
    }

    private Prescription mapPrescription(ResultSet rs) throws SQLException {
        Prescription pr = new Prescription(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("medicine"),
                rs.getString("dosage"),
                rs.getString("duration"),
                rs.getString("instructions"),
                rs.getString("date")
        );
        pr.setPatientName(rs.getString("patient_name"));
        pr.setDoctorName(rs.getString("doctor_name"));
        return pr;
    }
}
