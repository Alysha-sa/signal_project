package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Monitors patient data and generates alerts when predefined critical
 * conditions are met. Evaluates blood pressure, blood saturation, ECG,
 * and manually triggered alerts.
 */
public class AlertGenerator {

    private final DataStorage dataStorage;
    private final List<Alert> triggeredAlerts = new ArrayList<>();

    // strategy intances for the alert types
    private final AlertStrategy bloodPressureStrategy;
    private final AlertStrategy oxygenSaturationStrategy;
    private final AlertStrategy heartRateStrategy;

    // factory instances for the alert types
    private final AlertFactory bloodPressureFactory;
    private final AlertFactory bloodOxygenFactory;

    /**
     * Constructs an {@code AlertGenerator} with the specified {@code DataStorage}.
     *
     * @param dataStorage the system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;

        // initialization of strategies
        this.bloodPressureStrategy = new BloodPressureStrategy();
        this.oxygenSaturationStrategy = new OxygenSaturationStrategy();
        this.heartRateStrategy = new HeartRateStrategy();

        // initialization of factories
        this.bloodPressureFactory = new BloodPressureAlertFactory();
        this.bloodOxygenFactory = new BloodOxygenAlertFactory();
    }

    /**
     * Evaluates all alert conditions for the given patient.
     * Checks blood pressure, blood saturation, ECG, hypotensive hypoxemia,
     * and manually triggered alerts.
     *
     * @param patient the patient whose data will be evaluated
     */
    public void evaluateData(Patient patient) {
        // get all records for a wide time range
        List<PatientRecord> records = dataStorage.getRecords(
            patient.getPatientId(), 0, Long.MAX_VALUE);

        // delegate to strategy instances
        bloodPressureStrategy.checkAlert(patient, records, this::triggerAlert);
        oxygenSaturationStrategy.checkAlert(patient, records, this::triggerAlert);
        heartRateStrategy.checkAlert(patient, records, this::triggerAlert);

        // these two stay here since they cross multiple data types
        checkHypotensiveHypoxemiaAlert(patient, records);
        checkTriggeredAlerts(patient, records);
    }

    /**
     * Checks for the combined hypotensive hypoxemia condition.
     * Triggers an alert if systolic blood pressure is below 90 mmHg
     * and blood oxygen saturation is below 92% at the same time.
     *
     * @param patient the patient being evaluated
     * @param records all available records for the patient
     */
    private void checkHypotensiveHypoxemiaAlert(Patient patient, List<PatientRecord> records) {
        // check for low systolic blood pressure
        boolean lowSystolic = records.stream()
            .filter(r -> r.getRecordType().equals("SystolicPressure"))
            .anyMatch(r -> r.getMeasurementValue() < 90);

            // check for low blood oxygen saturation
        boolean lowSaturation = records.stream()
            .filter(r -> r.getRecordType().equals("Saturation"))
            .anyMatch(r -> r.getMeasurementValue() < 92);

        // evaluates if both apply
        if (lowSystolic && lowSaturation) {
            Alert alert = bloodOxygenFactory.createAlert(
                String.valueOf(patient.getPatientId()),
                "Hypotensive Hypoxemia Alert",
                System.currentTimeMillis());
            triggerAlert(new PriorityAlertDecorator(alert, "Critical"));
        }
    }

    /**
     * Checks for manually triggered alerts from nurses or patients.
     * Triggers an alert when the alert button has been pressed (status = "triggered")
     * and resolves it when the status changes to "resolved".
     *
     * @param patient the patient being evaluated
     * @param records all available records for the patient
     */
    private void checkTriggeredAlerts(Patient patient, List<PatientRecord> records) {
        String patientId = String.valueOf(patient.getPatientId());

        // filters for just alert records
        List<PatientRecord> alertRecords = records.stream()
            .filter(r -> r.getRecordType().equals("Alert"))
            .collect(Collectors.toList());

        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) { // 1.0 -> triggered
                // use blood pressure factory for creating manual alerts
                Alert alert = bloodPressureFactory.createAlert(
                    patientId, "Manual Alert Triggered", record.getTimestamp());
                triggerAlert(alert);
            }
        }
    }

    /**
     * Triggers an alert by printing it to the console. In a real
     * hospital system this would notify medical staff or log to a central
     * monitoring system.
     *
     * @param alert the alert containing details about the condition
     */
    private void triggerAlert(Alert alert) {
        triggeredAlerts.add(alert);
        System.out.println("Alert - Patient ID: " + alert.getPatientId()
            + ", Condition: " + alert.getCondition()
            + ", Timestamp: " + alert.getTimestamp());
    }

    /**
     * It returns all alerts that are triggered (used for testing)
     *
     * @return a list of all triggered alerts
     */
    public List<Alert> getTriggeredAlerts() {
        return triggeredAlerts;
    }

}
