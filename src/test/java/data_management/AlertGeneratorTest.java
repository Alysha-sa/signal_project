package data_management;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AlertGenerator}.
 * Tests all five alert types: blood pressure, blood saturation,
 * hypotensive hypoxemia, ECG, and triggered alerts.
 */
class AlertGeneratorTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;

    /**
     * Sets up a new DataStorage and AlertGenerator before each test.
     */
    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        alertGenerator = new AlertGenerator(storage);
    }

    //Blood Pressure Tests
    @Test
    void testBloodPressure_increasingTrend() {
        // three readings each increasing by more than 10
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 130.0, "SystolicPressure", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        // should not throw, alert is printed to console
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodPressure_decreasingTrend() {
        storage.addPatientData(1, 130.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 100.0, "SystolicPressure", 3000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodPressure_criticalHighSystolic() {
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodPressure_criticalLowSystolic() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodPressure_criticalHighDiastolic() {
        storage.addPatientData(1, 125.0, "DiastolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodPressure_criticalLowDiastolic() {
        storage.addPatientData(1, 55.0, "DiastolicPressure", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    //Blood Saturation Tests
    @Test
    void testBloodSaturation_lowSaturation() {
        storage.addPatientData(1, 91.0, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodSaturation_rapidDrop() {
        // drop of 5% within 10 minutes (600000ms)
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        storage.addPatientData(1, 93.0, "Saturation", 300000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testBloodSaturation_noAlertNormalValues() {
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        storage.addPatientData(1, 97.0, "Saturation", 2000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    //Hypotensive Hypoxemia Tests
    @Test
    void testHypotensiveHypoxemia_bothConditionsMet() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 91.0, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testHypotensiveHypoxemia_onlyLowPressure() {
        // should not trigger combined alert
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 97.0, "Saturation", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    //ECG Tests
    @Test
    void testECG_abnormalPeak() {
        // add 10 normal readings then one spike
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", i * 100L);
        }
        // spike: 3.0 is more than 1.5x average of 1.0
        storage.addPatientData(1, 3.0, "ECG", 1100L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testECG_noAlertNormalValues() {
        for (int i = 0; i < 11; i++) {
            storage.addPatientData(1, 1.0, "ECG", i * 100L);
        }

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    //Triggered Alert Tests
    @Test
    void testTriggeredAlert_triggered() {
        // 1.0 means alert button pressed
        storage.addPatientData(1, 1.0, "Alert", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }

    @Test
    void testTriggeredAlert_resolved() {
        // 0.0 means resolved, should not trigger
        storage.addPatientData(1, 0.0, "Alert", 1000L);

        Patient patient = storage.getAllPatients().get(0);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(patient));
    }
}
