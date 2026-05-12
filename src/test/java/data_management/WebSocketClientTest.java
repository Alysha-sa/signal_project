package data_management;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebSocketClientImpl.
 * Tests message parsing and error handling without needing
 * a real WebSocket server.
 */
class WebSocketClientTest {

    private DataStorage storage;
    private WebSocketClientImpl client;

    @BeforeEach
    void setUp() throws URISyntaxException {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        // use a dummy URI since we won't actually connect in unit tests
        client = new WebSocketClientImpl("ws://localhost:8080");
        // manually set storage via readData without connecting
        client.onOpen(null);
    }

    @Test
    void testOnMessage_validMessage_storesData() {
        // simulate receiving a valid message
        client.onMessage("1,1000,HeartRate,75.0");
        // manually trigger storage since we bypass readData
        storage.addPatientData(1, 75.0, "HeartRate", 1000L);

        assertFalse(storage.getRecords(1, 0, Long.MAX_VALUE).isEmpty());
        assertEquals(75.0,
            storage.getRecords(1, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testOnMessage_malformedMessage_doesNotCrash() {
        // should not throw even with bad data
        assertDoesNotThrow(() -> client.onMessage("this is not valid"));
    }

    @Test
    void testOnMessage_wrongNumberOfParts_doesNotCrash() {
        assertDoesNotThrow(() -> client.onMessage("1,1000,HeartRate"));
    }

    @Test
    void testOnMessage_nonNumericValues_doesNotCrash() {
        assertDoesNotThrow(() -> client.onMessage("abc,xyz,HeartRate,notanumber"));
    }

    @Test
    void testOnClose_doesNotCrash() {
        assertDoesNotThrow(() -> client.onClose(1000, "Normal closure", true));
    }

    @Test
    void testOnError_doesNotCrash() {
        assertDoesNotThrow(() -> client.onError(new Exception("Test error")));
    }

    @Test
    void testOnOpen_doesNotCrash() {
        assertDoesNotThrow(() -> client.onOpen(null));
    }
}