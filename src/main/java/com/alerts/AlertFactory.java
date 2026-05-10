package com.alerts;

/**
 * Base class for creating alerts.
 * Subclasses decide which specific type of alert to create based on the condition provided.
 */
public abstract class AlertFactory {

    /**
     * Creates and returns an alert for the given patient and condition.
     *
     * @param patientId the ID of the patient the alert is for
     * @param condition a description of the condition that triggered the alert
     * @param timestamp the time the alert was created, in milliseconds
     * @return a new Alert instance
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
