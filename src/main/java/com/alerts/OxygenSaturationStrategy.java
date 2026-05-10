package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy for checking alert conditions of blood oxygen saturation.
 * Checks for low saturation levels and critical drops within a short time window.
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodOxygenAlertFactory();

    /**
     * Checks records of oxygen saturation low levels and quick drops, and
     * triggers alerts when conditions are met.
     *
     * @param patient the patient being checked
     * @param records all records for this patient
     */
    @Override
    public void checkAlert(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> saturationRecords = records.stream()
            .filter(r -> r.getRecordType().equals("Saturation"))
            .collect(Collectors.toList());

        for (int i = 0; i < saturationRecords.size(); i++) {
            PatientRecord current = saturationRecords.get(i);
            double value = current.getMeasurementValue();

            // low saturation alert
            if (value < 92) {
                Alert alert = factory.createAlert(
                    String.valueOf(patient.getPatientId()),
                    "Low saturation: " + value + "%",
                    current.getTimestamp());
                System.out.println("ALERT - " + alert.getCondition());
            }

            // quick drop alert within 10 minutes
            for (int j = i + 1; j < saturationRecords.size(); j++) {
                PatientRecord later = saturationRecords.get(j);
                long timeDiff = later.getTimestamp() - current.getTimestamp();
                if (timeDiff <= 600000) {
                    double drop = value - later.getMeasurementValue();
                    if (drop >= 5) {
                        Alert alert = factory.createAlert(
                            String.valueOf(patient.getPatientId()),
                            "Rapid saturation drop: " + drop + "% in 10 minutes",
                            later.getTimestamp());
                        System.out.println("ALERT - " + alert.getCondition());
                    }
                } else {
                    break;
                }
            }
        }
    }
}
