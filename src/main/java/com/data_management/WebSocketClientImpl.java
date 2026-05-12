package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A WebSocket client that connects to a WebSocket server, so the 
 * patient data is streamed, each incoming message is parsed, and 
 * stored in the given DataStorage.
 *
 * Messages are expected in this format:
 * {@code patientId,timestamp,label,data}
 */
public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    private DataStorage dataStorage;

    /**
     * Creates a WebSocket client that will connect to the given server URI.
     *
     * @param serverUri the URI of the WebSocket server to connect to
     * @throws URISyntaxException if the URI is not valid
     */
    public WebSocketClientImpl(String serverUri) throws URISyntaxException {
        super(new URI(serverUri));
    }

    /**
     * Prints a confirmation message when there is a connection to the WebSocket server.
     *
     * @param handshake the server handshake details
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to WebSocket server");
    }

    /**
     * When a message is received from the server this method parses the
     * message and stores the data (in DataStorage).
     *
     * The expected format is: 
     * {@code patientId,timestamp,label,data}
     * Messages that dont have this format(abnormal) are logged and skipped
     *
     * @param message the message string that is received from the server
     */
    @Override
    public void onMessage(String message) {
        try {
            String[] parts = message.split(",");
            if (parts.length != 4) {
                System.err.println("Skipping abnormal message: " + message);
                return;
            }

            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            double measurementValue = Double.parseDouble(parts[3].trim());

            if (dataStorage != null) {
                dataStorage.addPatientData(patientId, measurementValue, label, timestamp);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing message: " + message + " - " + e.getMessage());
        }
    }

    /**
     * When the connection to the WebSocket server is broken/closed this 
     * method is called.
     *
     * @param code the closing code
     * @param reason the reason for closing
     * @param remote whether the closing was initiated by the remote host
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from WebSocket server. Reason: " + reason);
    }

    /**
     * Called when an error occurs in the WebSocket connection.
     * Logs the error without crashing the system.
     *
     * @param ex the exception that occurred
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    /**
     * Connects to the WebSocket server and starts reading data,
     * storing it in the provided DataStorage.
     *
     * @param dataStorage the storage where incoming data is saved
     * @throws IOException if there is no connection
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;
        try {
            this.connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebSocket connection was interrupted", e);
        }
    }

    /**
     * Starts a continuous stream with real-time data, and stores all 
     * incoming data in the given DataStorage until the connection is
     * broken.
     *
     * @param dataStorage the storage where incoming data is saved
     * @throws IOException if there is no connection
     */
    @Override
    public void startStreaming(DataStorage dataStorage) throws IOException {
        readData(dataStorage);
    }
}