package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

// changed the class name to UpperCamelCase
public class FileOutputStrategy implements OutputStrategy {

    // changed the variable name to lowerCamelCase
    // made field final so no changes occur after construction
    private final String baseDirectory;

    // changed to private field
    // changed variable name to lowerCamelcase
    private final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    // updated the constructor name to UpperCamelCase
    // updated the variable to lowerCamelCase
    public FileOutputStrategy(String baseDirectory) {

        // updated variable name to lowerCamelCase
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            // updated variable name to lowerCamelCase
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        // update variable names to lowerCamelCase
        // change variable name to lowerCamelCase
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString()); 

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                // updated variable name to match lowerCamelCase
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            // change variable name to lowerCamelCase
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}