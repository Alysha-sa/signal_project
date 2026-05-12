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

    /**
     * Constructs an {@code AlertGenerator} with the specified {@code DataStorage}.
     *
     * @param dataStorage the system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
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

        checkBloodPressureAlerts(patient, records);
        checkBloodSaturationAlerts(patient, records);
        checkHypotensiveHypoxemiaAlert(patient, records);
        checkECGAlerts(patient, records);
        checkTriggeredAlerts(patient, records);
    }

    /**
     * Checks a patient's blood pressure readings for two types of problems:
     * a consistent upward or downward trend (more than 10 mmHg change across
     * three consecutive readings), and readings that exceed safe limits
     * (systolic above 180 or below 90 mmHg, diastolic above 120 or below 60 mmHg).
     *
     * @param patient the patient being evaluated
     * @param records all available records for this patient
     */
    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> records) {
        // filter systolic and diastolic records separately
        List<PatientRecord> systolicRecords = records.stream()
            .filter(r -> r.getRecordType().equals("SystolicPressure"))
            .collect(Collectors.toList());

        List<PatientRecord> diastolicRecords = records.stream()
            .filter(r -> r.getRecordType().equals("DiastolicPressure"))
            .collect(Collectors.toList());

        // check trends for systolic
        checkPressureTrend(patient, systolicRecords, "Systolic");
        // check trends for diastolic
        checkPressureTrend(patient, diastolicRecords, "Diastolic");

        // check critical thresholds for systolic
        for (PatientRecord record : systolicRecords) {
            double value = record.getMeasurementValue();
            if (value > 180) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Critical Systolic Pressure High: " + value,
                    record.getTimestamp()));
            } else if (value < 90) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Critical Systolic Pressure Low: " + value,
                    record.getTimestamp()));
            }
        }

        // check critical thresholds for diastolic
        for (PatientRecord record : diastolicRecords) {
            double value = record.getMeasurementValue();
            if (value > 120) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Critical Diastolic Pressure High: " + value,
                    record.getTimestamp()));
            } else if (value < 60) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Critical Diastolic Pressure Low: " + value,
                    record.getTimestamp()));
            }
        }
    }

    /**
     * Checks for a consistent increasing or decreasing trend in blood pressure
     * readings. A trend alert is triggered if three consecutive readings each
     * change by more than 10 mmHg in the same direction.
     *
     * @param patient the patient being evaluated
     * @param records the list of systolic or diastolic pressure records
     * @param type a label indicating the type, "Systolic" or "Diastolic"
     */
    private void checkPressureTrend(Patient patient, List<PatientRecord> records, String type) {
        // needs at least 3 records to check a trend
        if (records.size() < 3) return;

        for (int i = 2; i < records.size(); i++) {
            double first = records.get(i - 2).getMeasurementValue();
            double second = records.get(i - 1).getMeasurementValue();
            double third = records.get(i).getMeasurementValue();

            // check increasing trend
            if (second - first > 10 && third - second > 10) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    type + " Blood Pressure Increasing Trend",
                    records.get(i).getTimestamp()));
            }

            // check decreasing trend
            if (first - second > 10 && second - third > 10) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    type + " Blood Pressure Decreasing Trend",
                    records.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Checks for low blood saturation and fast drop alerts.
     *
     * Triggers a low saturation alert if the value is below 92%.
     * Triggers a fast drop alert if saturation drops by 5% or more
     * within a 10 minutes.
     *
     * @param patient the patient being evaluated
     * @param records all available records for the patient
     */
    private void checkBloodSaturationAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> saturationRecords = records.stream()
            .filter(r -> r.getRecordType().equals("Saturation"))
            .collect(Collectors.toList());

        for (int i = 0; i < saturationRecords.size(); i++) {
            PatientRecord current = saturationRecords.get(i);
            double value = current.getMeasurementValue();

            // low saturation alert
            if (value < 92) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Low Blood Saturation: " + value + "%",
                    current.getTimestamp()));
            }

            // fast drop alert (check within 10 minutes (= 600000 ms))
            for (int j = i + 1; j < saturationRecords.size(); j++) {
                PatientRecord later = saturationRecords.get(j);
                long timeDiff = later.getTimestamp() - current.getTimestamp();

                if (timeDiff <= 600000) {
                    double drop = value - later.getMeasurementValue();
                    if (drop >= 5) {
                        triggerAlert(new Alert(
                            String.valueOf(patient.getPatientId()),
                            "Rapid Blood Saturation Drop: " + drop + "% in 10 minutes",
                            later.getTimestamp()));
                    }
                } else {
                    break; // break when longer than 10 minutes
                }
            }
        }
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
            triggerAlert(new Alert(
                String.valueOf(patient.getPatientId()),
                "Hypotensive Hypoxemia Alert",
                System.currentTimeMillis()));
        }
    }

    /**
     * Looks for abnormal spikes in a patient's ECG data by comparing each
     * reading against the average of the previous 10 readings(in this case). 
     * If a reading is more than 50% above that average, an alert is triggered.
     *
     * @param patient the patient being evaluated
     * @param records all available records for this patient
     */
    private void checkECGAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = records.stream()
            .filter(r -> r.getRecordType().equals("ECG"))
            .collect(Collectors.toList());

        // need enough records to compute a meaningful average
        int windowSize = 10;
        if (ecgRecords.size() < windowSize) return;

        for (int i = windowSize; i < ecgRecords.size(); i++) {
            // calculate average of the last windowSize records
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double average = sum / windowSize;

            double current = ecgRecords.get(i).getMeasurementValue();

            // trigger alert if current value exceeds average by more than 50%
            if (Math.abs(current) > Math.abs(average) * 1.5) {
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Abnormal ECG Peak Detected: " + current,
                    ecgRecords.get(i).getTimestamp()));
            }
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
        List<PatientRecord> alertRecords = records.stream()
            .filter(r -> r.getRecordType().equals("Alert"))
            .collect(Collectors.toList());

        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) {
                // 1.0 means triggered
                triggerAlert(new Alert(
                    String.valueOf(patient.getPatientId()),
                    "Manual Alert Triggered",
                    record.getTimestamp()));
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
        System.out.println("ALERT - Patient ID: " + alert.getPatientId()
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
