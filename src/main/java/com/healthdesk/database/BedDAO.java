package com.healthdesk.database;

import com.healthdesk.model.Bed;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bed entity.
 */
public class BedDAO {

    public List<Bed> getAllBeds() {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT b.*, p.name AS patient_name " +
                     "FROM Bed b " +
                     "LEFT JOIN Patient p ON b.patient_id = p.id " +
                     "ORDER BY b.ward ASC, b.id ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBed(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BedDAO] Error fetching beds: " + e.getMessage());
        }
        return list;
    }

    public List<Bed> getAvailableBeds() {
        List<Bed> list = new ArrayList<>();
        String sql = "SELECT b.*, NULL AS patient_name " +
                     "FROM Bed b " +
                     "WHERE b.status = 'Available' " +
                     "ORDER BY b.ward ASC, b.id ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBed(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BedDAO] Error fetching available beds: " + e.getMessage());
        }
        return list;
    }

    public Bed getBedByPatient(int patientId) {
        String sql = "SELECT b.*, p.name AS patient_name " +
                     "FROM Bed b " +
                     "JOIN Patient p ON b.patient_id = p.id " +
                     "WHERE b.patient_id = ? LIMIT 1;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapBed(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BedDAO] Error fetching bed by patient: " + e.getMessage());
        }
        return null;
    }

    public boolean assignBed(String bedId, int patientId) {
        // Release any bed previously held by this patient first
        String releasePrior = "UPDATE Bed SET status = 'Available', patient_id = NULL WHERE patient_id = ?;";
        String assign = "UPDATE Bed SET status = 'Occupied', patient_id = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement p1 = conn.prepareStatement(releasePrior)) {
                p1.setInt(1, patientId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement(assign)) {
                p2.setInt(1, patientId);
                p2.setString(2, bedId);
                return p2.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[BedDAO] Assign bed error: " + e.getMessage());
            return false;
        }
    }

    public boolean releaseBed(String bedId) {
        String sql = "UPDATE Bed SET status = 'Available', patient_id = NULL WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bedId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BedDAO] Release bed error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(String bedId, String status) {
        String sql = "UPDATE Bed SET status = ?, patient_id = CASE WHEN ? != 'Occupied' THEN NULL ELSE patient_id END WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, status);
            pstmt.setString(3, bedId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BedDAO] Update status error: " + e.getMessage());
            return false;
        }
    }

    public boolean addBed(String id, String ward, String status) {
        String sql = "INSERT INTO Bed (id, ward, status, patient_id) VALUES (?, ?, ?, NULL);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, ward);
            pstmt.setString(3, status);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BedDAO] Add bed error: " + e.getMessage());
            return false;
        }
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM Bed;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {}
        return 0;
    }

    public int getOccupiedCount() {
        String sql = "SELECT COUNT(*) FROM Bed WHERE status = 'Occupied';";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {}
        return 0;
    }

    public int getAvailableCount() {
        String sql = "SELECT COUNT(*) FROM Bed WHERE status = 'Available';";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {}
        return 0;
    }

    private Bed mapBed(ResultSet rs) throws SQLException {
        Bed bed = new Bed();
        bed.setId(rs.getString("id"));
        bed.setWard(rs.getString("ward"));
        bed.setStatus(rs.getString("status"));
        int pId = rs.getInt("patient_id");
        if (!rs.wasNull()) {
            bed.setPatientId(pId);
            bed.setPatientName(rs.getString("patient_name"));
        } else {
            bed.setPatientId(null);
            bed.setPatientName(null);
        }
        return bed;
    }
}
