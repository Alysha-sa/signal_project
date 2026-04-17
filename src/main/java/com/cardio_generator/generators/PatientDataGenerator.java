package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for simulating patient data
 * Implementations for this data are different types of health data
 * (e.g. ECG, blood pressure)
 */

public interface PatientDataGenerator {
    /**
     * Generates data for a specific patient and sends it to the given output strategy
     * 
     * @param patientId identifier of the patient
     * @param outputStrategy strategy used to output the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
