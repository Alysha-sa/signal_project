package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Patient#getRecords(long, long)}.
 */
class PatientTest {

    @Test
    void testGetRecords_withinRange() {
        Patient patient = new Patient(1);
        patient.addRecord(75.0, "HeartRate", 1000L);
        patient.addRecord(80.0, "HeartRate", 2000L);
        patient.addRecord(85.0, "HeartRate", 3000L);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
        assertEquals(80.0, records.get(1).getMeasurementValue());
    }

    @Test
    void testGetRecords_noRecordsInRange() {
        Patient patient = new Patient(1);
        patient.addRecord(75.0, "HeartRate", 1000L);

        List<PatientRecord> records = patient.getRecords(5000L, 9000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecords_exactBoundary() {
        Patient patient = new Patient(1);
        patient.addRecord(75.0, "HeartRate", 1000L);
        patient.addRecord(80.0, "HeartRate", 2000L);

        // both boundary timestamps should be inclusive
        List<PatientRecord> records = patient.getRecords(1000L, 1000L);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecords_emptyPatient() {
        Patient patient = new Patient(1);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertTrue(records.isEmpty());
    }
}
