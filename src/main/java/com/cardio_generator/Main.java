package com.cardio_generator;

import com.data_management.DataStorage;

/**
 * {@code Main} is the starting point for the Cardiovascular Health Monitoring System.
 * It decides whether to run {@link DataStorage} or the {@link HealthDataSimulator} 
 * based on what arguments are passed in.
 */
public class Main {

    /**
     * Main method that selects which class to run.
     * Pass "DataStorage" as the first argument to run DataStorage,
     * otherwise HealthDataSimulator runs by default.
     *
     * @param args command-line arguments (pass "DataStorage" to run DataStorage)
     * @throws Exception if an error occurs during execution
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            DataStorage.main(new String[]{});
        } else {
            HealthDataSimulator.main(args);
        }
    }
}
