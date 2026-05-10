package com.alerts;

/**
 * class that creates blood oxygen alerts
 */
public class BloodOxygenAlertFactory extends AlertFactory{
    /**
     * creates blood oxygen alerts for a given patient when 
     * significant changes in blood oxygen levels are detected.
     * 
     * @param patientId the ID of the patient that the alert is for
     * @param condition the oxygen condition that triggered the alert
     * @param timestamp the time the alert was created
     * @return a new alert for blood oxygen conditions
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "Blood Oxygen Alert: " + condition, timestamp);
    }
}
