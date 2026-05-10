package data_management;

import com.alerts.AlertGenerator;
import com.alerts.BloodPressureStrategy;
import com.alerts.HeartRateStrategy;
import com.alerts.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Strategy pattern implementations.
 */
class AlertStrategyTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    // Blood Pressure Strategy
    @Test
    void testBloodPressureStrategy_criticalHigh() {
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        assertDoesNotThrow(() ->
            new BloodPressureStrategy().checkAlert(patient, records));
    }

    @Test
    void testBloodPressureStrategy_criticalLow() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        assertDoesNotThrow(() ->
            new BloodPressureStrategy().checkAlert(patient, records));
    }

    @Test
    void testBloodPressureStrategy_increasingTrend() {
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 130.0, "SystolicPressure", 3000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        assertDoesNotThrow(() ->
            new BloodPressureStrategy().checkAlert(patient, records));
    }

    // Oxygen Saturation Strategy
    @Test
    void testOxygenSaturationStrategy_lowSaturation() {
        storage.addPatientData(1, 91.0, "Saturation", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        assertDoesNotThrow(() ->
            new OxygenSaturationStrategy().checkAlert(patient, records));
    }

    @Test
    void testOxygenSaturationStrategy_rapidDrop() {
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        storage.addPatientData(1, 93.0, "Saturation", 300000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        assertDoesNotThrow(() ->
            new OxygenSaturationStrategy().checkAlert(patient, records));
    }

    // Heart Rate Strategy
    @Test
    void testHeartRateStrategy_abnormalPeak() {
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", i * 100L);
        }
        storage.addPatientData(1, 3.0, "ECG", 1100L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        assertDoesNotThrow(() ->
            new HeartRateStrategy().checkAlert(patient, records));
    }

    @Test
    void testHeartRateStrategy_notEnoughRecords() {
        storage.addPatientData(1, 1.0, "ECG", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        // should not throw even with insufficient records
        assertDoesNotThrow(() ->
            new HeartRateStrategy().checkAlert(patient, records));
    }
}