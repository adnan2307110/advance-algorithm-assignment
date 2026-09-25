package com.healthdesk.service;

import com.healthdesk.database.AppointmentDAO;
import com.healthdesk.model.Appointment;
import javafx.application.Platform;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Multithreaded service that periodically inspects upcoming appointments in a background thread
 * and generates live system notifications without blocking the JavaFX UI.
 */
public class NotificationService {
    private static NotificationService instance;
    private final AppointmentDAO appointmentDAO;
    private final ScheduledExecutorService scheduler;
    private final List<String> notifications;
    private final Set<Integer> notifiedAppointmentIds;
    private final List<Consumer<String>> listeners;
    private boolean running = false;

    private NotificationService() {
        this.appointmentDAO = new AppointmentDAO();
        this.notifications = new CopyOnWriteArrayList<>();
        this.notifiedAppointmentIds = Collections.synchronizedSet(new HashSet<>());
        this.listeners = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Appointment-Notifier-Thread");
            t.setDaemon(true); // Allows clean JVM shutdown
            return t;
        });
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    /**
     * Starts the periodic background checker thread.
     */
    public synchronized void start() {
        if (running) return;
        running = true;

        // Run immediately, then check every 30 seconds in the background thread
        scheduler.scheduleAtFixedRate(this::checkUpcomingAppointments, 2, 30, TimeUnit.SECONDS);
        System.out.println("[NotificationService] Background appointment notifier thread started.");
    }

    /**
     * The background task runnable checking today's appointments.
     */
    private void checkUpcomingAppointments() {
        try {
            List<Appointment> todayList = appointmentDAO.getTodayAppointments();
            LocalTime now = LocalTime.now();

            for (Appointment appt : todayList) {
                if ("Scheduled".equalsIgnoreCase(appt.getStatus()) && !notifiedAppointmentIds.contains(appt.getId())) {
                    String timeStr = appt.getTime(); // e.g. "10:30" or "10:30 AM"
                    try {
                        LocalTime apptTime = parseTime(timeStr);
                        // Notify if appointment is within next 2 hours or currently occurring
                        if (apptTime != null && !now.isAfter(apptTime.plusMinutes(15))) {
                            String msg = "Appointment: " + appt.getPatientName() +
                                    " with " + appt.getDoctorName() + " is scheduled at " + timeStr + ".";
                            notifiedAppointmentIds.add(appt.getId());
                            publishNotification(msg);
                        }
                    } catch (Exception ex) {
                        // Fallback notice
                        String msg = "Upcoming appointment: " + appt.getPatientName() +
                                " with " + appt.getDoctorName() + " at " + timeStr + ".";
                        notifiedAppointmentIds.add(appt.getId());
                        publishNotification(msg);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[NotificationService] Error checking appointments: " + e.getMessage());
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        String clean = timeStr.trim();
        try {
            if (clean.matches("\\d{1,2}:\\d{2}")) {
                return LocalTime.parse(clean, DateTimeFormatter.ofPattern("H:mm"));
            }
            if (clean.toUpperCase().contains("AM") || clean.toUpperCase().contains("PM")) {
                return LocalTime.parse(clean.toUpperCase(), DateTimeFormatter.ofPattern("h:mm a"));
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Publishes a notification to the internal thread-safe log and dispatches to listeners
     * via Platform.runLater to guarantee UI thread safety.
     */
    public void publishNotification(String message) {
        notifications.add(0, message); // Latest first
        // Limit notification history size to 50
        if (notifications.size() > 50) {
            notifications.remove(notifications.size() - 1);
        }

        // Dispatch safely to JavaFX thread
        try {
            Platform.runLater(() -> {
                for (Consumer<String> listener : listeners) {
                    try {
                        listener.accept(message);
                    } catch (Exception e) {
                        System.err.println("[NotificationService] Listener error: " + e.getMessage());
                    }
                }
            });
        } catch (IllegalStateException e) {
            // If JavaFX toolkit is not initialized yet (e.g. unit tests)
            for (Consumer<String> listener : listeners) {
                listener.accept(message);
            }
        }
    }

    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        scheduler.shutdownNow();
    }
}
