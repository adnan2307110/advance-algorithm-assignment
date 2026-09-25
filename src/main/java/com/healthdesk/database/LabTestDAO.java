package com.healthdesk.database;

import com.healthdesk.model.LabTest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for LabTest entity.
 */
public class LabTestDAO {

    public List<LabTest> getAllTests() {
        List<LabTest> list = new ArrayList<>();
        String sql = "SELECT lt.*, p.name AS patient_name, d.name AS doctor_name " +
                     "FROM LabTest lt " +
                     "JOIN Patient p ON lt.patient_id = p.id " +
                     "JOIN Doctor d ON lt.doctor_id = d.id " +
                     "ORDER BY lt.id DESC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapLabTest(rs));
            }
        } catch (SQLException e) {
            System.err.println("[LabTestDAO] Error fetching tests: " + e.getMessage());
        }
        return list;
    }

    public List<LabTest> getTestsByPatient(int patientId) {
        List<LabTest> list = new ArrayList<>();
        String sql = "SELECT lt.*, p.name AS patient_name, d.name AS doctor_name " +
                     "FROM LabTest lt " +
                     "JOIN Patient p ON lt.patient_id = p.id " +
                     "JOIN Doctor d ON lt.doctor_id = d.id " +
                     "WHERE lt.patient_id = ? " +
                     "ORDER BY lt.id DESC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLabTest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[LabTestDAO] Error fetching by patient: " + e.getMessage());
        }
        return list;
    }

    public List<LabTest> getPendingTests() {
        List<LabTest> list = new ArrayList<>();
        String sql = "SELECT lt.*, p.name AS patient_name, d.name AS doctor_name " +
                     "FROM LabTest lt " +
                     "JOIN Patient p ON lt.patient_id = p.id " +
                     "JOIN Doctor d ON lt.doctor_id = d.id " +
                     "WHERE lt.status != 'Completed' " +
                     "ORDER BY lt.id ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapLabTest(rs));
            }
        } catch (SQLException e) {
            System.err.println("[LabTestDAO] Error fetching pending tests: " + e.getMessage());
        }
        return list;
    }

    public boolean addTest(LabTest test) {
        String sql = "INSERT INTO LabTest (patient_id, doctor_id, test_name, result, status, date) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, test.getPatientId());
            pstmt.setInt(2, test.getDoctorId());
            pstmt.setString(3, test.getTestName());
            pstmt.setString(4, test.getResult());
            pstmt.setString(5, test.getStatus() != null ? test.getStatus() : "Requested");
            pstmt.setString(6, test.getDate());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        test.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[LabTestDAO] Add test error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateResult(int testId, String result, String status) {
        String sql = "UPDATE LabTest SET result = ?, status = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, result);
            pstmt.setString(2, status);
            pstmt.setInt(3, testId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LabTestDAO] Update result error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int testId, String status) {
        String sql = "UPDATE LabTest SET status = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, testId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LabTestDAO] Update status error: " + e.getMessage());
            return false;
        }
    }

    public int getPendingCount() {
        String sql = "SELECT COUNT(*) FROM LabTest WHERE status != 'Completed';";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {}
        return 0;
    }

    private LabTest mapLabTest(ResultSet rs) throws SQLException {
        LabTest t = new LabTest(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("test_name"),
                rs.getString("result"),
                rs.getString("status"),
                rs.getString("date")
        );
        t.setPatientName(rs.getString("patient_name"));
        t.setDoctorName(rs.getString("doctor_name"));
        return t;
    }
}
