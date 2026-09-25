package com.healthdesk.database;

import com.healthdesk.model.Appointment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Appointment entity.
 */
public class AppointmentDAO {

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                     "FROM Appointment a " +
                     "JOIN Patient p ON a.patient_id = p.id " +
                     "JOIN Doctor d ON a.doctor_id = d.id " +
                     "ORDER BY a.date DESC, a.time ASC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Error fetching appointments: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                     "FROM Appointment a " +
                     "JOIN Patient p ON a.patient_id = p.id " +
                     "JOIN Doctor d ON a.doctor_id = d.id " +
                     "WHERE a.doctor_id = ? " +
                     "ORDER BY a.date DESC, a.time ASC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Error fetching by doctor: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                     "FROM Appointment a " +
                     "JOIN Patient p ON a.patient_id = p.id " +
                     "JOIN Doctor d ON a.doctor_id = d.id " +
                     "WHERE a.patient_id = ? " +
                     "ORDER BY a.date DESC, a.time ASC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Error fetching by patient: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getTodayAppointments() {
        String today = LocalDate.now().toString();
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                     "FROM Appointment a " +
                     "JOIN Patient p ON a.patient_id = p.id " +
                     "JOIN Doctor d ON a.doctor_id = d.id " +
                     "WHERE a.date = ? " +
                     "ORDER BY a.time ASC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Error fetching today's appointments: " + e.getMessage());
        }
        return list;
    }

    /**
     * Prevents duplicate appointments for the same doctor on the same date and time.
     */
    public boolean isDuplicateAppointment(int doctorId, String date, String time, int excludeId) {
        String sql = "SELECT COUNT(*) FROM Appointment WHERE doctor_id = ? AND date = ? AND time = ? AND id != ? AND status != 'Cancelled';";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date.trim());
            pstmt.setString(3, time.trim());
            pstmt.setInt(4, excludeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Duplicate check error: " + e.getMessage());
        }
        return false;
    }

    public boolean addAppointment(Appointment appt) {
        if (isDuplicateAppointment(appt.getDoctorId(), appt.getDate(), appt.getTime(), 0)) {
            return false;
        }
        String sql = "INSERT INTO Appointment (patient_id, doctor_id, date, time, status) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, appt.getPatientId());
            pstmt.setInt(2, appt.getDoctorId());
            pstmt.setString(3, appt.getDate());
            pstmt.setString(4, appt.getTime());
            pstmt.setString(5, appt.getStatus());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        appt.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Add appointment error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateAppointmentStatus(int id, String status) {
        String sql = "UPDATE Appointment SET status = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Update status error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAppointment(Appointment appt) {
        if (isDuplicateAppointment(appt.getDoctorId(), appt.getDate(), appt.getTime(), appt.getId())) {
            return false;
        }
        String sql = "UPDATE Appointment SET patient_id = ?, doctor_id = ?, date = ?, time = ?, status = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appt.getPatientId());
            pstmt.setInt(2, appt.getDoctorId());
            pstmt.setString(3, appt.getDate());
            pstmt.setString(4, appt.getTime());
            pstmt.setString(5, appt.getStatus());
            pstmt.setInt(6, appt.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Update error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAppointment(int id) {
        String sql = "DELETE FROM Appointment WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Delete error: " + e.getMessage());
            return false;
        }
    }

    public int getTodayCount() {
        String today = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) FROM Appointment WHERE date = ? AND status != 'Cancelled';";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AppointmentDAO] Today count error: " + e.getMessage());
        }
        return 0;
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("date"),
                rs.getString("time"),
                rs.getString("status")
        );
        appt.setPatientName(rs.getString("patient_name"));
        appt.setDoctorName(rs.getString("doctor_name"));
        appt.setDoctorSpecialization(rs.getString("specialization"));
        return appt;
    }
}
