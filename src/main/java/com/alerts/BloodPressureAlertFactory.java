package com.alerts;

/**
 * class that creates bloodpressure alerts
 */
public class BloodPressureAlertFactory extends AlertFactory{
    /**
     * creates blood pressure alert for a given patient when 
     * blood pressure anomalies are detected.
     * 
     * @param patientId the ID of the patient that the alert is for
     * @param condition the blood pressure condition that triggered the alert
     * @param timestamp the time the alert was created in milliseconds
     * @return a new alert for blood pressure conditions
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "Blood Pressure Alert:" + condition, timestamp);
    }

}
