package com.healthdesk.api;

import com.healthdesk.model.HealthInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles HTTP requests, JSON parsing, and mapping into HealthInfo model objects.
 * Executes in background threads to avoid freezing the JavaFX UI.
 */
public class HealthAPI {
    private static final String DEFAULT_API_URL = "https://raw.githubusercontent.com/adnan-hossain/health-api/main/health_topics.json";
    private final HttpClient httpClient;

    public HealthAPI() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    /**
     * Asynchronously fetches health information from the API endpoint.
     * Completes on a background thread and parses the JSON response.
     */
    public CompletableFuture<List<HealthInfo>> fetchHealthInfoAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(DEFAULT_API_URL))
                        .timeout(Duration.ofSeconds(4))
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
                    return parseHealthInfoJson(response.body());
                }
            } catch (Exception e) {
                System.out.println("[HealthAPI] Remote API not reachable (" + e.getMessage() + "). Loading bundled health knowledge base.");
            }
            // Fallback to embedded standard clinical JSON response
            return parseHealthInfoJson(getBundledHealthJson());
        });
    }

    /**
     * Parses JSON string into a list of HealthInfo Java objects.
     */
    public List<HealthInfo> parseHealthInfoJson(String jsonString) {
        List<HealthInfo> list = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray topics = root.getJSONArray("health_topics");

            for (int i = 0; i < topics.length(); i++) {
                JSONObject obj = topics.getJSONObject(i);
                String name = obj.optString("name", "Unknown Topic");
                String normalRange = obj.optString("normal_range", "N/A");
                String description = obj.optString("description", "");
                String category = obj.optString("category", "General");

                HealthInfo info = new HealthInfo(name, normalRange, description, category);
                list.add(info);
            }
        } catch (Exception e) {
            System.err.println("[HealthAPI] JSON parsing error: " + e.getMessage());
            // If raw array format
            try {
                JSONArray arr = new JSONArray(jsonString);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    list.add(new HealthInfo(
                            obj.optString("name", "Topic"),
                            obj.optString("normal_range", "N/A"),
                            obj.optString("description", ""),
                            obj.optString("category", "General")
                    ));
                }
            } catch (Exception ignored) {}
        }
        return list;
    }

    /**
     * High quality standard clinical health knowledge encoded as JSON
     * ensuring immediate reliability during offline examinations and university vivas.
     */
    public String getBundledHealthJson() {
        return "{\n" +
                "  \"source\": \"HealthDesk Medical Knowledge Base API\",\n" +
                "  \"health_topics\": [\n" +
                "    {\n" +
                "      \"name\": \"Blood Pressure\",\n" +
                "      \"normal_range\": \"120/80 mmHg\",\n" +
                "      \"category\": \"Cardiovascular\",\n" +
                "      \"description\": \"Systolic pressure under 120 mmHg and diastolic pressure under 80 mmHg indicate healthy arterial tension. Elevated readings (130-139 / 80-89) signify stage 1 hypertension requiring dietary modification and lifestyle monitoring.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Fasting Blood Glucose\",\n" +
                "      \"normal_range\": \"70 – 99 mg/dL\",\n" +
                "      \"category\": \"Endocrinology\",\n" +
                "      \"description\": \"Measures blood sugar level after fasting for at least 8 hours. Levels between 100-125 mg/dL indicate prediabetes, while 126 mg/dL or higher on two separate tests confirms diabetes mellitus.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Resting Heart Rate\",\n" +
                "      \"normal_range\": \"60 – 100 bpm\",\n" +
                "      \"category\": \"Cardiovascular\",\n" +
                "      \"description\": \"Resting pulse rate in healthy adults. Well-conditioned athletes may have normal resting rates below 50 bpm (bradycardia). Tachycardia occurs when resting rate consistently exceeds 100 bpm.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Oxygen Saturation (SpO2)\",\n" +
                "      \"normal_range\": \"95% – 100%\",\n" +
                "      \"category\": \"Pulmonary\",\n" +
                "      \"description\": \"Fraction of oxygen-saturated hemoglobin relative to total hemoglobin in arterial blood. Values below 92% signal hypoxemia and require immediate clinical assessment or supplemental oxygen.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Body Mass Index (BMI)\",\n" +
                "      \"normal_range\": \"18.5 – 24.9 kg/m²\",\n" +
                "      \"category\": \"General Health\",\n" +
                "      \"description\": \"Key weight-to-height nutritional indicator: Below 18.5 is underweight, 18.5-24.9 is normal weight, 25.0-29.9 is overweight, and 30.0 or greater indicates clinical obesity.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Total Cholesterol\",\n" +
                "      \"normal_range\": \"< 200 mg/dL\",\n" +
                "      \"category\": \"Lipid Profile\",\n" +
                "      \"description\": \"Sum of blood cholesterol content including HDL, LDL, and VLDL. Levels from 200 to 239 mg/dL are borderline high; 240 mg/dL or above poses higher risk of coronary artery disease.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Body Temperature\",\n" +
                "      \"normal_range\": \"97.8°F – 99.1°F (36.5°C – 37.3°C)\",\n" +
                "      \"category\": \"Vital Signs\",\n" +
                "      \"description\": \"Core human baseline temperature. A reading above 100.4°F (38.0°C) is clinically diagnosed as pyrexia (fever), often signaling an immunological defense response against infection.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Hemoglobin (Hb)\",\n" +
                "      \"normal_range\": \"13.8 – 17.2 g/dL (M) / 12.1 – 15.1 g/dL (F)\",\n" +
                "      \"category\": \"Hematology\",\n" +
                "      \"description\": \"Iron-containing oxygen-transport metalloprotein in red blood cells. Low hemoglobin indicates anemia, causing fatigue, paleness, and shortness of breath upon mild exertion.\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Testing HealthAPI HTTP & JSON Integration      ");
        System.out.println("==================================================");
        HealthAPI api = new HealthAPI();
        try {
            System.out.println("Fetching health topics asynchronously...");
            List<HealthInfo> list = api.fetchHealthInfoAsync().get();
            System.out.println("Successfully fetched " + list.size() + " topics:");
            for (HealthInfo hi : list) {
                System.out.printf("  * %-26s | Range: %-28s | Category: %s\n",
                        hi.getName(), hi.getNormalRange(), hi.getCategory());
            }
        } catch (Exception e) {
            System.err.println("API test error: " + e.getMessage());
        }
        System.out.println("==================================================");
    }
}

