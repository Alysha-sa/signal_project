package data_management;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Unit tests for {@link DataStorage}.
 */

class DataStorageTest {

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
    }

    @Test
    void testAddAndGetRecords() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecords_noPatientFound() {
        DataStorage storage = DataStorage.getInstance();

        // patient 99 was never added
        List<PatientRecord> records = storage.getRecords(99, 1714376789050L, 1714376789051L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetAllPatients_empty() {
        DataStorage storage = DataStorage.getInstance();
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testGetAllPatients_multiplePatients() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "HeartRate", 1714376789050L);
        storage.addPatientData(2, 200.0, "HeartRate", 1714376789050L);

        assertEquals(2, storage.getAllPatients().size());
    }
}
