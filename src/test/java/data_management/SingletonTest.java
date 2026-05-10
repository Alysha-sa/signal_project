package data_management;

import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Singleton pattern implementation.
 */
class SingletonTest {

    @Test
    void testDataStorage_sameInstanceReturned() {
        DataStorage instance1 = DataStorage.getInstance();
        DataStorage instance2 = DataStorage.getInstance();

        // identical instances
        assertSame(instance1, instance2);
    }

    @Test
    void testDataStorage_sharedState() {
        DataStorage instance1 = DataStorage.getInstance();
        instance1.addPatientData(99, 75.0, "HeartRate", 1000L);

        DataStorage instance2 = DataStorage.getInstance();

        // data added through instance1 should be visible through instance2
        assertFalse(instance2.getRecords(99, 0, Long.MAX_VALUE).isEmpty());
    }
}
