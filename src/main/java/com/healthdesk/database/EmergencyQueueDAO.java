package com.healthdesk.database;

import com.healthdesk.model.EmergencyPatient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Emergency Queue entity.
 */
public class EmergencyQueueDAO {

    public List<EmergencyPatient> getAllEmergencyPatients() {
        List<EmergencyPatient> list = new ArrayList<>();
        String sql = "SELECT eq.*, p.name AS patient_name " +
                     "FROM EmergencyQueue eq " +
                     "JOIN Patient p ON eq.patient_id = p.id " +
                     "ORDER BY eq.priority ASC, eq.arrival_time ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEmergencyPatient(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmergencyQueueDAO] Error fetching queue: " + e.getMessage());
        }
        return list;
    }

    public List<EmergencyPatient> getWaitingPatients() {
        List<EmergencyPatient> list = new ArrayList<>();
        String sql = "SELECT eq.*, p.name AS patient_name " +
                     "FROM EmergencyQueue eq " +
                     "JOIN Patient p ON eq.patient_id = p.id " +
                     "WHERE eq.status = 'Waiting' " +
                     "ORDER BY eq.priority ASC, eq.arrival_time ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEmergencyPatient(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EmergencyQueueDAO] Error fetching waiting: " + e.getMessage());
        }
        return list;
    }

    public boolean addEmergencyPatient(EmergencyPatient ep) {
        String sql = "INSERT INTO EmergencyQueue (patient_id, priority, condition, status, arrival_time) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, ep.getPatientId());
            pstmt.setInt(2, ep.getPriority());
            pstmt.setString(3, ep.getCondition());
            pstmt.setString(4, ep.getStatus());
            pstmt.setLong(5, ep.getArrivalTime() > 0 ? ep.getArrivalTime() : System.currentTimeMillis());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        ep.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[EmergencyQueueDAO] Add error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE EmergencyQueue SET status = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EmergencyQueueDAO] Update status error: " + e.getMessage());
            return false;
        }
    }

    public boolean remove(int id) {
        String sql = "DELETE FROM EmergencyQueue WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EmergencyQueueDAO] Delete error: " + e.getMessage());
            return false;
        }
    }

    public int getWaitingCount() {
        String sql = "SELECT COUNT(*) FROM EmergencyQueue WHERE status = 'Waiting';";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[EmergencyQueueDAO] Waiting count error: " + e.getMessage());
        }
        return 0;
    }

    private EmergencyPatient mapEmergencyPatient(ResultSet rs) throws SQLException {
        return new EmergencyPatient(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getString("patient_name"),
                rs.getInt("priority"),
                rs.getString("condition"),
                rs.getString("status"),
                rs.getLong("arrival_time")
        );
    }
}
