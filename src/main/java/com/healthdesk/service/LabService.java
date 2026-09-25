package com.healthdesk.service;

import com.healthdesk.database.LabTestDAO;
import com.healthdesk.model.LabTest;
import javafx.application.Platform;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Multithreaded service managing laboratory test requests and sample processing simulations.
 * Uses an ExecutorService worker pool to execute diagnostics without freezing the UI.
 */
public class LabService {
    private static LabService instance;
    private final LabTestDAO labTestDAO;
    private final ExecutorService workerPool;
    private final Random random = new Random();

    private LabService() {
        this.labTestDAO = new LabTestDAO();
        this.workerPool = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "Lab-Simulation-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public static synchronized LabService getInstance() {
        if (instance == null) {
            instance = new LabService();
        }
        return instance;
    }

    /**
     * Submits a new laboratory test request.
     */
    public boolean requestTest(LabTest test) {
        test.setStatus("Requested");
        boolean success = labTestDAO.addTest(test);
        if (success) {
            NotificationService.getInstance().publishNotification(
                    "Lab request submitted: " + test.getTestName() + " for " + test.getPatientName()
            );
        }
        return success;
    }

    /**
     * Simulates laboratory sample processing in a background worker thread.
     * Transitions status: Requested -> Processing -> Completed.
     */
    public void processTestSimulated(LabTest test, Consumer<LabTest> onStatusUpdate, Consumer<LabTest> onComplete) {
        workerPool.submit(() -> {
            try {
                // Step 1: Transition to Processing
                test.setStatus("Processing");
                labTestDAO.updateStatus(test.getId(), "Processing");
                dispatchToUI(onStatusUpdate, test);

                // Step 2: Background processing delay (simulating chemical reagents / centrifuge)
                Thread.sleep(3500);

                // Step 3: Generate realistic clinical results if not provided
                String generatedResult = test.getResult();
                if (generatedResult == null || generatedResult.trim().isEmpty()) {
                    generatedResult = generateDefaultResult(test.getTestName());
                }
                test.setResult(generatedResult);
                test.setStatus("Completed");

                // Step 4: Persist completed state to database
                labTestDAO.updateResult(test.getId(), generatedResult, "Completed");

                // Step 5: Notify user and dispatch UI callback
                NotificationService.getInstance().publishNotification(
                        "LAB COMPLETED: " + test.getTestName() + " for " + test.getPatientName() + " is ready."
                );
                dispatchToUI(onComplete, test);

            } catch (InterruptedException e) {
                System.err.println("[LabService] Lab simulation interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[LabService] Error during lab simulation: " + e.getMessage());
            }
        });
    }

    /**
     * Updates manual result entered by a Lab Technician and marks completed.
     */
    public boolean enterManualResult(int testId, String result) {
        boolean ok = labTestDAO.updateResult(testId, result, "Completed");
        if (ok) {
            NotificationService.getInstance().publishNotification(
                    "Lab Technician saved results for Test #" + testId
            );
        }
        return ok;
    }

    private <T> void dispatchToUI(Consumer<T> callback, T value) {
        if (callback == null) return;
        try {
            Platform.runLater(() -> callback.accept(value));
        } catch (IllegalStateException e) {
            callback.accept(value);
        }
    }

    /**
     * Generates standard clinical metrics based on requested test category.
     */
    public String generateDefaultResult(String testName) {
        String lower = testName.toLowerCase();
        if (lower.contains("blood") || lower.contains("cbc")) {
            double hb = 12.0 + (random.nextDouble() * 4.0); // 12.0 - 16.0
            int wbc = 4500 + random.nextInt(6000); // 4500 - 10500
            int plt = 150000 + random.nextInt(250000); // 150000 - 400000
            return String.format("Hemoglobin: %.1f g/dL\nWBC: %,d /uL\nPlatelet: %,d /uL\nRBC: 4.8 M/uL\nStatus: Normal reference range",
                    hb, wbc, plt);
        } else if (lower.contains("lipid") || lower.contains("cholesterol")) {
            int tc = 160 + random.nextInt(70);
            int hdl = 40 + random.nextInt(30);
            int ldl = 90 + random.nextInt(50);
            int tg = 110 + random.nextInt(80);
            return String.format("Total Cholesterol: %d mg/dL\nHDL: %d mg/dL\nLDL: %d mg/dL\nTriglycerides: %d mg/dL",
                    tc, hdl, ldl, tg);
        } else if (lower.contains("sugar") || lower.contains("glucose") || lower.contains("diabetes")) {
            int fbs = 75 + random.nextInt(50);
            double hba1c = 5.2 + (random.nextDouble() * 1.5);
            return String.format("Fasting Blood Glucose: %d mg/dL\nHbA1c: %.1f%%\nInterpretation: %s",
                    fbs, hba1c, (fbs > 100 ? "Borderline elevated" : "Normal"));
        } else if (lower.contains("x-ray") || lower.contains("imaging") || lower.contains("chest")) {
            return "Chest Radiograph (PA View):\n- Lungs clear, no active parenchymal infiltrate\n- Cardiac silhouette within normal limits\n- Costophrenic angles sharp and clear\nImpression: Unremarkable chest radiograph";
        } else if (lower.contains("liver") || lower.contains("lft")) {
            return "ALT (SGPT): 28 U/L (Normal < 45)\nAST (SGOT): 24 U/L (Normal < 40)\nTotal Bilirubin: 0.8 mg/dL (Normal 0.2-1.2)\nAlbumin: 4.2 g/dL (Normal 3.5-5.0)";
        } else if (lower.contains("urine") || lower.contains("urinalysis")) {
            return "Color: Pale Yellow\nClarity: Clear\nSpecific Gravity: 1.018\npH: 6.0\nProtein: Negative\nGlucose: Negative\nWBC: 0-2 /HPF";
        } else {
            return "Standard Diagnostic Panel Result:\nSpecimen analyzed successfully.\nAll baseline parameters conform to standard clinical reference limits.";
        }
    }

    public void shutdown() {
        workerPool.shutdownNow();
    }
}
