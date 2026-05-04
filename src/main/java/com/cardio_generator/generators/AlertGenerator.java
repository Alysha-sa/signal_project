package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated alert data for patients based on a Poisson process model.
 * Each patient can be in either an active alert (triggered) or no alert (resolved)state.
 * State transitions are governed by random probabilities.
 *
 * <p>Alerts are triggered using an exponential distribution with a fixed rate
 * lambda, and once active, have a 90% chance of resolving each period.
 */

public class AlertGenerator implements PatientDataGenerator {

    private static final Random randomGenerator = new Random();

    /**
     * Keeps track of the alert state for each patient.
     * {@code true} means alert is active (triggered).
     * {@code false} means no active alert (resolved).
     */
    private boolean[] alertState; // renamed to lowerCamelCase

    /**
     * Creates an {@code AlertGenerator} for the given number of patients.
     * All patients start with no active alert.
     *
     * @param patientCount the total number of patients to track
     */
    public AlertGenerator(int patientCount) {
        alertState = new boolean[patientCount + 1];
    }

    /**
     * Generates and outputs an alert status update for the given patient.
     *
     * <p>If the patient currently has an active alert, there is a 90% probability
     * it will be resolved in this period. If no alert is active, a new alert may
     * be triggered based on a Poisson arrival model with rate lambda = 0.1.
     *
     * <p>Output label is {@code "Alert"} with data value either {@code "triggered"}
     * or {@code "resolved"}.
     *
     * @param patientId identifier of patient
     * @param outputStrategy the strategy used to output the generated alert data
     * @throws Exception if an unexpected error occurs during data generation
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertState[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertState[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertState[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
