package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for checking alert conditions related to blood pressure.
 * Checks for critical threshholds and trends in systolic and diastolic blood
 * pressure data.
 */
public class BloodPressureStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodPressureAlertFactory();

    /**
     * Checks blood pressure records for thresholds and trends,
     * triggering alerts when conditions are met.
     *
     * @param patient the patient being checked
     * @param records all records for this patient
     */
    @Override
    public void checkAlert(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolic = records.stream()
            .filter(r -> r.getRecordType().equals("SystolicPressure"))
            .collect(Collectors.toList());

        List<PatientRecord> diastolic = records.stream()
            .filter(r -> r.getRecordType().equals("DiastolicPressure"))
            .collect(Collectors.toList());

        checkThresholds(patient, systolic, 180, 90, "Systolic");
        checkThresholds(patient, diastolic, 120, 60, "Diastolic");
        checkTrend(patient, systolic, "Systolic");
        checkTrend(patient, diastolic, "Diastolic");
    }

    /**
     * Checks if any reading exceeds the high or low threshold.
     *
     * @param patient the patient being checked
     * @param records the list of pressure records to check
     * @param high the high threshold
     * @param low the low threshold
     * @param type label showing systolic or diastolic
     */
    private void checkThresholds(Patient patient, List<PatientRecord> records,
                                  double high, double low, String type) {
        for (PatientRecord record : records) {
            double value = record.getMeasurementValue();
            if (value > high) {
                Alert alert = factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    type + " pressure critically high: " + value,
                    record.getTimestamp());
                System.out.println("ALERT - " + alert.getCondition());
            } else if (value < low) {
                Alert alert = factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    type + " pressure critically low: " + value,
                    record.getTimestamp());
                System.out.println("ALERT - " + alert.getCondition());
            }
        }
    }

    /**
     * Looks  for a trend, so consistent increase or decrease, using three
     * consecutive readings and checking if the threshold is met.
     *
     * @param patient the patient being checked
     * @param records the list of pressure records to check
     * @param type label indicating systolic or diastolic
     */
    private void checkTrend(Patient patient, List<PatientRecord> records, String type) {
        if (records.size() < 3) return;
        for (int i = 2; i < records.size(); i++) {
            double first = records.get(i - 2).getMeasurementValue();
            double second = records.get(i - 1).getMeasurementValue();
            double third = records.get(i).getMeasurementValue();

            if (second - first > 10 && third - second > 10) { // threshold of 10 mmHg
                Alert alert = factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    type + " pressure increasing trend",
                    records.get(i).getTimestamp());
                System.out.println("ALERT - " + alert.getCondition());
            } else if (first - second > 10 && second - third > 10) {
                Alert alert = factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    type + " pressure decreasing trend",
                    records.get(i).getTimestamp());
                System.out.println("ALERT - " + alert.getCondition());
            }
        }
    }
}
