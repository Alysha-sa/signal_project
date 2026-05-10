package com.alerts;

import com.data_management.Patient;
import java.util.List;

/**
 * An interface for checking alert strategies. Every method 
 * checks for a specific health metric and alerts when needed.
 */
public interface AlertStrategy{

    /**
     * checks the records of the patient and triggers an alert if
     * a condition is met.
     * 
     * @param patient the patient that is checked
     * @param records the records of the patient that is being evaluated
     */
    void checkAlert(Patient patient, List<com.data_management.PatientRecord> records);
}
