package data_management;

import com.alerts.Alert;
import com.alerts.AlertFactory;
import com.alerts.BloodOxygenAlertFactory;
import com.alerts.BloodPressureAlertFactory;
import com.alerts.ECGAlertFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Factory Method pattern implementation.
 */
class AlertFactoryTest {

    @Test
    void testBloodPressureAlertFactory_createsCorrectAlert() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "High Systolic", 1000L);

        assertEquals("1", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Blood Pressure Alert"));
        assertTrue(alert.getCondition().contains("High Systolic"));
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    void testBloodOxygenAlertFactory_createsCorrectAlert() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("2", "Low Saturation", 2000L);

        assertEquals("2", alert.getPatientId());
        assertTrue(alert.getCondition().contains("Blood Oxygen Alert"));
        assertTrue(alert.getCondition().contains("Low Saturation"));
        assertEquals(2000L, alert.getTimestamp());
    }

    @Test
    void testECGAlertFactory_createsCorrectAlert() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("3", "Abnormal Peak", 3000L);

        assertEquals("3", alert.getPatientId());
        assertTrue(alert.getCondition().contains("ECG Alert"));
        assertTrue(alert.getCondition().contains("Abnormal Peak"));
        assertEquals(3000L, alert.getTimestamp());
    }

    @Test
    void testFactory_differentFactoriesProduceDifferentAlerts() {
        AlertFactory bpFactory = new BloodPressureAlertFactory();
        AlertFactory o2Factory = new BloodOxygenAlertFactory();

        Alert bpAlert = bpFactory.createAlert("1", "High", 1000L);
        Alert o2Alert = o2Factory.createAlert("1", "High", 1000L);

        assertNotEquals(bpAlert.getCondition(), o2Alert.getCondition());
    }
}
