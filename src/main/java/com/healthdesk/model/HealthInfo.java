package com.healthdesk.model;

/**
 * Model class for external health information topics parsed from JSON.
 */
public class HealthInfo {
    private String name;
    private String normalRange;
    private String description;
    private String category;

    public HealthInfo() {}

    public HealthInfo(String name, String normalRange, String description, String category) {
        this.name = name;
        this.normalRange = normalRange;
        this.description = description;
        this.category = category;
    }

    public HealthInfo(String name, String normalRange, String description) {
        this(name, normalRange, description, "General Health");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name + " [" + normalRange + "]";
    }
}
