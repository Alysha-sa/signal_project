package com.data_management;

import java.io.IOException; 
/**
 * How data is read into the system, in this case either from a static source or a
 * WebSocket connection. Uses the readData method for one time reads and the
 * startStreaming method for a continuous read of data.
 */
public interface DataReader {
    /**
     * Reads data from a specified source and stores it in the data storage.
     * 
     * @param dataStorage where the data is stored
     * @throws IOException if there is an error reading the data
     */
    void readData(DataStorage dataStorage) throws IOException;

    /**
     * Handles real-time data, continuously receives data from the WebSocket server.
     * 
     * @param dataStorage where the data is stored
     * @throws IOException if the connection fails
     */
    default void startStreaming(DataStorage dataStorage) throws IOException {
        readData(dataStorage);
    }
}

