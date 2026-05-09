package com.alerts;

/**
 * Simulates an alert that is generated when a patient's health data meets 
 * the predefined conditions. 
 * Each alert contains information about the patient, the condition that 
 * triggered it, and when it occurred.
 */
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Constructs a new Alert with the specified details.
     *
     * @param patientId unique identifier of the patient
     * @param condition description of the condition that triggered the alert
     * @param timestamp the time at which the alert was generated in milliseconds
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    /**
     * Returns the patient ID connected with this alert.
     *
     * @return the patient ID
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Returns the condition that triggered this alert.
     *
     * @return the condition description
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the timestamp of when this alert was generated.
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
}