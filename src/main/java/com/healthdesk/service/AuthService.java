package com.healthdesk.service;

import com.healthdesk.database.UserDAO;
import com.healthdesk.model.User;

/**
 * Service managing user session and role-based permissions.
 */
public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password, String role) {
        User user = userDAO.authenticate(username, password, role);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean canManageDoctors() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean canManageUsers() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean canManagePatients() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("receptionist");
    }

    public boolean canBookAppointments() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("receptionist");
    }

    public boolean canManageBeds() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("receptionist");
    }

    public boolean canPrescribe() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("doctor");
    }

    public boolean canRequestLab() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("doctor");
    }

    public boolean canPerformLabTests() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("lab technician") || role.equals("labtechnician");
    }

    public boolean canManageBilling() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("receptionist");
    }

    public boolean canManageEmergency() {
        if (currentUser == null) return false;
        String role = currentUser.getRole().toLowerCase();
        return role.equals("admin") || role.equals("receptionist") || role.equals("doctor");
    }
}
