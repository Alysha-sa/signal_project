package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for checking heart rate (ECG) alert conditions.
 * Uses a sliding window average to detect abnormal peaks in ECG data.
 */
public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory factory = new ECGAlertFactory();

    /**
     * Checks ECG records for abnormal peaks, does this by comparing
     * each reading to the average of the window size, in this case 
     * the previous 10 readings. Also triggers an alert if the readings
     * are higher than the average by 50%
     *
     * @param patient the patient being checked
     * @param records all records for this patient
     */
    @Override
    public void checkAlert(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = records.stream()
            .filter(r -> r.getRecordType().equals("ECG"))
            .collect(Collectors.toList());

        int windowSize = 10; // the 10 readings
        if (ecgRecords.size() < windowSize) return;

        for (int i = windowSize; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double average = sum / windowSize;
            double current = ecgRecords.get(i).getMeasurementValue();

            if (Math.abs(current) > Math.abs(average) * 1.5) {
                Alert alert = factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Abnormal ECG peak: " + current,
                    ecgRecords.get(i).getTimestamp());
                System.out.println("ALERT - " + alert.getCondition());
            }
        }
    }
}
