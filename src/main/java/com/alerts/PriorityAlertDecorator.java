package com.alerts;

/**
 * A decorator that tags a priority to an alert, urgent ->
 * medical staff know it needs immediate attention.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    private final String priorityLevel;

    /**
     * Creates a priority alert decorator for the given alert.
     *
     * @param decoratedAlert the alert to wrap
     * @param priorityLevel the priority level that the alert gets
     */
    public PriorityAlertDecorator(Alert decoratedAlert, String priorityLevel) {
        super(decoratedAlert);
        this.priorityLevel = priorityLevel;
    }

    /**
     * Returns the condition with the priority level, so the urgency
     * of the alert is indicated clearly.
     *
     * @return the condition string with priority information
     */
    @Override
    public String getCondition() {
        return "[Priority: " + priorityLevel + "] " + decoratedAlert.getCondition();
    }
}