package com.healthdesk.database;

import com.healthdesk.model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bill entity.
 */
public class BillDAO {

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, p.name AS patient_name " +
                     "FROM Bill b " +
                     "JOIN Patient p ON b.patient_id = p.id " +
                     "ORDER BY b.id DESC;";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBill(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Error fetching bills: " + e.getMessage());
        }
        return list;
    }

    public List<Bill> getBillsByPatient(int patientId) {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, p.name AS patient_name " +
                     "FROM Bill b " +
                     "JOIN Patient p ON b.patient_id = p.id " +
                     "WHERE b.patient_id = ? " +
                     "ORDER BY b.id DESC;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBill(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Error fetching by patient: " + e.getMessage());
        }
        return list;
    }

    public boolean addBill(Bill bill) {
        // Ensure total is computed
        bill.calculateTotal();
        String sql = "INSERT INTO Bill (patient_id, consultation_fee, lab_fee, bed_fee, medicine_fee, other_fee, total, date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, bill.getPatientId());
            pstmt.setDouble(2, bill.getConsultationFee());
            pstmt.setDouble(3, bill.getLabFee());
            pstmt.setDouble(4, bill.getBedFee());
            pstmt.setDouble(5, bill.getMedicineFee());
            pstmt.setDouble(6, bill.getOtherFee());
            pstmt.setDouble(7, bill.getTotal());
            pstmt.setString(8, bill.getDate());
            pstmt.setString(9, bill.getStatus());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        bill.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[BillDAO] Add bill error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int billId, String status) {
        String sql = "UPDATE Bill SET status = ? WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, billId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BillDAO] Update status error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBill(int billId) {
        String sql = "DELETE FROM Bill WHERE id = ?;";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BillDAO] Delete error: " + e.getMessage());
            return false;
        }
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getDouble("consultation_fee"),
                rs.getDouble("lab_fee"),
                rs.getDouble("bed_fee"),
                rs.getDouble("medicine_fee"),
                rs.getDouble("other_fee"),
                rs.getDouble("total"),
                rs.getString("date"),
                rs.getString("status")
        );
        bill.setPatientName(rs.getString("patient_name"));
        return bill;
    }
}
