package com.cardio_generator.outputs;
/**
 * Interface that defines a strategy for outputting generated patient data.
 * Implementations of this interface determine how and where data is stored.
 */

public interface OutputStrategy {
    /**
     * Outputs generated patient data using a specific strategy.
     *
     * @param patientId identifier of the patient
     * @param timestamp the time at which the data was generated
     * @param label the type of data being output
     * @param data the actual generated data value
     */
    void output(int patientId, long timestamp, String label, String data);
}
