package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.function.Consumer;

/**
 * An interface for checking alert strategies. Every method 
 * checks for a specific health metric and alerts when needed.
 */
public interface AlertStrategy {

    /**
     * checks the records of the patient and triggers an alert if
     * a condition is met.
     * 
     * @param patient the patient that is checked
     * @param records the records of the patient that is being evaluated
     * @param alertCallback a callback that accepts an alert when a condition is met
     */
    void checkAlert(Patient patient, List<PatientRecord> records, Consumer<Alert> alertCallback);
}
