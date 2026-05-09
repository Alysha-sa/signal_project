package data_management;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FileDataReader}.
 */
class FileDataReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadData_validFile() throws IOException {
        // create a temp file with valid data
        Path file = tempDir.resolve("HeartRate.txt");
        Files.writeString(file,
            "Patient ID: 1, Timestamp: 1714376789050, Label: HeartRate, Data: 75.0\n" +
            "Patient ID: 1, Timestamp: 1714376789051, Label: HeartRate, Data: 80.0\n"
        );

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
        assertEquals(80.0, records.get(1).getMeasurementValue());
    }

    // skips lines that differ from expected format
    @Test
    void testReadData_malformedLinesSkipped() throws IOException {
        Path file = tempDir.resolve("HeartRate.txt");
        Files.writeString(file,
            "this is not valid data\n" +
            "Patient ID: 1, Timestamp: 1714376789050, Label: HeartRate, Data: 75.0\n"
        );

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        // only the valid line should be stored
        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789050L);
        assertEquals(1, records.size());
    }

    @Test
    void testReadData_directoryNotFound() {
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader("directory not found");

        assertThrows(IOException.class, () -> reader.readData(storage));
    }

    // throw exception when empty directory
    @Test
    void testReadData_emptyDirectory() throws IOException {
        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        // no patients should be added
        assertTrue(storage.getAllPatients().isEmpty());
    }
}
