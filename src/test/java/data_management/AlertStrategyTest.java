package data_management;

import com.alerts.Alert;
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

        List<Alert> triggered = new java.util.ArrayList<>();
        new BloodPressureStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(
            triggered.stream().anyMatch(a ->
                a.getCondition().contains("Critical Systolic Pressure High")
                && a.getCondition().contains("Blood Pressure Alert")),
            "Expected a critical high systolic alert"
        );
    }

    @Test
    void testBloodPressureStrategy_criticalLow() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        List<Alert> triggered = new java.util.ArrayList<>();
        new BloodPressureStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(
            triggered.stream().anyMatch(a ->
                a.getCondition().contains("Critical Systolic Pressure Low")
                && a.getCondition().contains("Blood Pressure Alert")),
            "Expected a critical low systolic alert"
        );
    }

    @Test
    void testBloodPressureStrategy_increasingTrend() {
        storage.addPatientData(1, 100.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 115.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 130.0, "SystolicPressure", 3000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        List<Alert> triggered = new java.util.ArrayList<>();
        new BloodPressureStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(
            triggered.stream().anyMatch(a -> a.getCondition().contains("Increasing Trend")),
            "Expected a systolic increasing trend alert"
        );
    }

    // Oxygen Saturation Strategy
    @Test
    void testOxygenSaturationStrategy_lowSaturation() {
        storage.addPatientData(1, 91.0, "Saturation", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        List<Alert> triggered = new java.util.ArrayList<>();
        new OxygenSaturationStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(
            triggered.stream().anyMatch(a ->
                a.getCondition().contains("Low Blood Saturation")
                && a.getCondition().contains("Blood Oxygen Alert")),
            "Expected a low saturation alert"
        );
    }

    @Test
    void testOxygenSaturationStrategy_rapidDrop() {
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        storage.addPatientData(1, 93.0, "Saturation", 300000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        List<Alert> triggered = new java.util.ArrayList<>();
        new OxygenSaturationStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(
            triggered.stream().anyMatch(a ->
                a.getCondition().contains("Quick Blood Saturation Drop")
                && a.getCondition().contains("Blood Oxygen Alert")),
            "Expected a rapid saturation drop alert"
        );
    }

    // Heart Rate Strategy
    @Test
    void testHeartRateStrategy_abnormalPeak() {
        for (int i = 0; i < 10; i++) {
            storage.addPatientData(1, 1.0, "ECG", i * 100L);
        }
        // spike: 3.0 is more than 1.5x average of 1.0
        storage.addPatientData(1, 3.0, "ECG", 1100L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        List<Alert> triggered = new java.util.ArrayList<>();
        new HeartRateStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(
            triggered.stream().anyMatch(a ->
                a.getCondition().contains("Abnormal ECG Peak Detected")
                && a.getCondition().contains("ECG Alert")),
            "Expected an abnormal ECG peak alert"
        );
    }

    @Test
    void testHeartRateStrategy_notEnoughRecords() {
        storage.addPatientData(1, 1.0, "ECG", 1000L);
        Patient patient = storage.getAllPatients().get(0);
        List<com.data_management.PatientRecord> records =
            storage.getRecords(1, 0, Long.MAX_VALUE);

        List<Alert> triggered = new java.util.ArrayList<>();
        // should not throw even with insufficient records
        new HeartRateStrategy().checkAlert(patient, records, triggered::add);

        assertTrue(triggered.isEmpty(),
            "Expected no alert when there are not enough ECG records"
        );
    }
}