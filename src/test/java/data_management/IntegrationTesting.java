package data_management;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientImpl;
import com.alerts.AlertGenerator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WebSocketClientImpl.
 * Sets up a WebSocket server which tests that the format is 
 * correct, and that it correctly receives, parses, and stores
 * the data.
 */
class WebSocketIntegrationTest {

    private TestWebSocketServer server;
    private WebSocketClientImpl client;
    private DataStorage storage;
    private static final int PORT = 8887;

    /**
     * Simple test WebSocket server that sends predefined messages
     * to clients when connected.
     */
    static class TestWebSocketServer extends WebSocketServer {

        private String messageToSend;

        public TestWebSocketServer(int port) {
            super(new InetSocketAddress(port));
        }

        public void setMessageToSend(String message) {
            this.messageToSend = message;
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            // send the test message (when client connects)
            if (messageToSend != null) {
                conn.send(messageToSend);
            }
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

        @Override
        public void onMessage(WebSocket conn, String message) {}

        @Override
        public void onError(WebSocket conn, Exception ex) {}

        @Override
        public void onStart() {}
    }

    @BeforeEach
    void setUp() throws Exception {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        server = new TestWebSocketServer(PORT);
        server.start();
        // time for the server to start up
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null && !client.isClosed()) {
            client.closeBlocking();
        }
        server.stop();
    }

    // Integration Tests

    @Test
    void testIntegration_validMessage_storedInDataStorage() throws Exception {
        server.setMessageToSend("1,1000,HeartRate,75.0");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        client.readData(storage);

        // time for the message to be processed
        Thread.sleep(200);

        assertFalse(storage.getRecords(1, 0, Long.MAX_VALUE).isEmpty());
        assertEquals(75.0,
            storage.getRecords(1, 0, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testIntegration_multipleMessages_allStored() throws Exception {
        // sends multiple messages
        server.setMessageToSend("1,1000,HeartRate,75.0");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        client.readData(storage);
        Thread.sleep(100);

        // sends a message directly
        for (WebSocket conn : server.getConnections()) {
            conn.send("2,2000,BloodPressure,120.0");
        }
        Thread.sleep(200);

        assertFalse(storage.getRecords(1, 0, Long.MAX_VALUE).isEmpty());
        assertFalse(storage.getRecords(2, 0, Long.MAX_VALUE).isEmpty());
    }

    // Error Handling Tests

    @Test
    void testErrorHandling_disformedMessage_doesNotCrash() throws Exception {
        server.setMessageToSend("this is not valid data");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        // should not throw even with disformed data
        assertDoesNotThrow(() -> client.readData(storage));
        Thread.sleep(200);

        // dont store anyhting
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testErrorHandling_wrongNumberOfParts_doesNotCrash() throws Exception {
        server.setMessageToSend("1,1000,HeartRate");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        assertDoesNotThrow(() -> client.readData(storage));
        Thread.sleep(200);

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testErrorHandling_nonNumericValues_doesNotCrash() throws Exception {
        server.setMessageToSend("abc,xyz,HeartRate,notanumber");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        assertDoesNotThrow(() -> client.readData(storage));
        Thread.sleep(200);

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testErrorHandling_connectionLoss_handledGracefully() throws Exception {
        server.setMessageToSend("1,1000,HeartRate,75.0");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        client.readData(storage);
        Thread.sleep(100);

        // stops the server (simulated when connection is broken)
        server.stop();
        Thread.sleep(200);

        // client should fix this themselves
        assertDoesNotThrow(() -> client.onError(new Exception("Connection lost")));
    }

    @Test
    void testIntegration_criticalSystolicPressure_alertCreated() throws Exception {
        server.setMessageToSend("1,1000,SystolicPressure,185.0");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        client.readData(storage);
        Thread.sleep(200);

        com.alerts.AlertGenerator alertGenerator = new com.alerts.AlertGenerator(storage);
        for (com.data_management.Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }

        // check if alert was created
        assertFalse(alertGenerator.getTriggeredAlerts().isEmpty());

        // check if the correct condition is triggered
        boolean correctAlertFound = alertGenerator.getTriggeredAlerts().stream()
            .anyMatch(a -> a.getCondition().contains("Critical Systolic Pressure High"));
        assertTrue(correctAlertFound);

        // check if the patient ID is correct
        assertEquals("1", alertGenerator.getTriggeredAlerts().get(0).getPatientId());
    }

    @Test
    void testIntegration_lowSaturation_alertCreated() throws Exception {
        server.setMessageToSend("1,1000,Saturation,88.0");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        client.readData(storage);
        Thread.sleep(200);

        com.alerts.AlertGenerator alertGenerator = new com.alerts.AlertGenerator(storage);
        for (com.data_management.Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }

        // check that an alert was created
        assertFalse(alertGenerator.getTriggeredAlerts().isEmpty());

        // check that the correct condition was triggered
        boolean correctAlertFound = alertGenerator.getTriggeredAlerts().stream()
            .anyMatch(a -> a.getCondition().contains("Low Blood Saturation"));
        assertTrue(correctAlertFound);

        // check that the alert contains the right saturation value
        boolean correctValueFound = alertGenerator.getTriggeredAlerts().stream()
            .anyMatch(a -> a.getCondition().contains("88.0"));
        assertTrue(correctValueFound);
    }

    @Test
    void testIntegration_hypotensiveHypoxemia_alertCreated() throws Exception {
        // sends both low systolic and low saturation through WebSocket
        server.setMessageToSend("1,1000,SystolicPressure,85.0");

        client = new WebSocketClientImpl("ws://localhost:" + PORT);
        client.readData(storage);
        Thread.sleep(100);

        // sends saturation directly
        for (org.java_websocket.WebSocket conn : server.getConnections()) {
            conn.send("1,2000,Saturation,88.0");
        }
        Thread.sleep(200);

        com.alerts.AlertGenerator alertGenerator = new com.alerts.AlertGenerator(storage);
        for (com.data_management.Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }

        // checks that the combined alert was triggered
        boolean hypoxemiaAlertFound = alertGenerator.getTriggeredAlerts().stream()
            .anyMatch(a -> a.getCondition().contains("Hypotensive Hypoxemia"));
        assertTrue(hypoxemiaAlertFound);
    }
}
