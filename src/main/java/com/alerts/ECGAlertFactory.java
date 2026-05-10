package com.alerts;

/**
 * class that creates ECG alerts
 */
public class ECGAlertFactory extends AlertFactory {
    /**
     * creates alert for ECG conditions, such as irregular heart rates 
     * and rhythms
     * 
     * @param patientId the ID of the patient that the alert is for
     * @param condition the condition that triggered the alert
     * @param timestamp the time the alert was created
     * @return a new alert for ECG conditions
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "ECG Alert: " + condition, timestamp);
    }
}
