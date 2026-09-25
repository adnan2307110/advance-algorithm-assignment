package com.healthdesk.service;

import com.healthdesk.database.EmergencyQueueDAO;
import com.healthdesk.model.EmergencyPatient;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Service managing the hospital emergency triage queue using Java's PriorityQueue.
 * Ensures thread-safe access with synchronization.
 */
public class EmergencyService {
    private static EmergencyService instance;
    private final EmergencyQueueDAO queueDAO;
    private final PriorityQueue<EmergencyPatient> priorityQueue;

    private EmergencyService() {
        this.queueDAO = new EmergencyQueueDAO();
        this.priorityQueue = new PriorityQueue<>();
        reloadFromDatabase();
    }

    public static synchronized EmergencyService getInstance() {
        if (instance == null) {
            instance = new EmergencyService();
        }
        return instance;
    }

    /**
     * Reloads all active waiting patients from the database into the Java PriorityQueue.
     */
    public synchronized void reloadFromDatabase() {
        priorityQueue.clear();
        List<EmergencyPatient> waiting = queueDAO.getWaitingPatients();
        priorityQueue.addAll(waiting);
    }

    /**
     * Enqueues a new patient into the triage queue.
     * Uses Java PriorityQueue ordering and persists to SQLite.
     */
    public synchronized boolean enqueuePatient(int patientId, String patientName, int priority, String condition) {
        EmergencyPatient ep = new EmergencyPatient(patientId, patientName, priority, condition, "Waiting");
        boolean success = queueDAO.addEmergencyPatient(ep);
        if (success) {
            priorityQueue.offer(ep);
            NotificationService.getInstance().publishNotification(
                    "EMERGENCY ALERT: " + patientName + " added with Priority " + priority + " (" + condition + ")"
            );
        }
        return success;
    }

    /**
     * Processes and retrieves the highest priority patient from the queue.
     * The highest priority patient (Priority 1 > 2 > 3) is polled first.
     */
    public synchronized EmergencyPatient processNextPatient() {
        if (priorityQueue.isEmpty()) {
            reloadFromDatabase();
        }
        if (priorityQueue.isEmpty()) {
            return null;
        }

        // Poll highest priority patient
        EmergencyPatient next = priorityQueue.poll();
        next.setStatus("In Treatment");
        queueDAO.updateStatus(next.getId(), "In Treatment");

        NotificationService.getInstance().publishNotification(
                "TRIAGE: Patient " + next.getPatientName() + " (Priority " + next.getPriority() + ") sent for immediate treatment."
        );
        return next;
    }

    /**
     * Peeks at the highest-priority patient waiting in queue without removing.
     */
    public synchronized EmergencyPatient peekNextPatient() {
        return priorityQueue.peek();
    }

    /**
     * Returns an ordered snapshot of currently waiting patients.
     */
    public synchronized List<EmergencyPatient> getWaitingSnapshot() {
        PriorityQueue<EmergencyPatient> copy = new PriorityQueue<>(priorityQueue);
        List<EmergencyPatient> orderedList = new ArrayList<>();
        while (!copy.isEmpty()) {
            orderedList.add(copy.poll());
        }
        return orderedList;
    }

    /**
     * Returns full queue history from database.
     */
    public List<EmergencyPatient> getFullHistory() {
        return queueDAO.getAllEmergencyPatients();
    }

    public synchronized int getWaitingCount() {
        return priorityQueue.size();
    }

    public synchronized boolean updateStatus(int id, String status) {
        boolean updated = queueDAO.updateStatus(id, status);
        if (updated) {
            reloadFromDatabase();
        }
        return updated;
    }

    public synchronized boolean removePatient(int id) {
        boolean removed = queueDAO.remove(id);
        if (removed) {
            reloadFromDatabase();
        }
        return removed;
    }
}
