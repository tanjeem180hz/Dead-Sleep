package com.deadsleep.app.model;

/**
 * Represents an intelligent routine recommendation produced by RoutineAdvisor.
 */
public class RoutineSuggestion {
    private final String id;
    private final String title;
    private final String description;
    private final String ruleType; // BEDTIME_DRIFT, BLOCKED_SPIKE, SNOOZE_OVERUSE, SLEEP_DEBT
    private final String actionLabel;
    private boolean isApplied;

    public RoutineSuggestion(String id, String title, String description, String ruleType, String actionLabel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ruleType = ruleType;
        this.actionLabel = actionLabel;
        this.isApplied = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRuleType() { return ruleType; }
    public String getActionLabel() { return actionLabel; }
    public boolean isApplied() { return isApplied; }
    public void setApplied(boolean applied) { isApplied = applied; }
}
