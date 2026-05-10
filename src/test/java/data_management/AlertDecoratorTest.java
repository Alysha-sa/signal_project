package data_management;

import com.alerts.Alert;
import com.alerts.PriorityAlertDecorator;
import com.alerts.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Decorator pattern implementations.
 */
class AlertDecoratorTest {

    @Test
    void testRepeatedAlertDecorator_addsRepeatInfo() {
        Alert base = new Alert("1", "High Blood Pressure", 1000L);
        Alert repeated = new RepeatedAlertDecorator(base, 3);

        assertTrue(repeated.getCondition().contains("High Blood Pressure"));
        assertTrue(repeated.getCondition().contains("Repeated 3 time(s)"));
    }

    @Test
    void testRepeatedAlertDecorator_preservesPatientId() {
        Alert base = new Alert("1", "High Blood Pressure", 1000L);
        Alert repeated = new RepeatedAlertDecorator(base, 1);

        assertEquals("1", repeated.getPatientId());
        assertEquals(1000L, repeated.getTimestamp());
    }

    @Test
    void testPriorityAlertDecorator_addsPriorityInfo() {
        Alert base = new Alert("2", "Low Oxygen", 2000L);
        Alert priority = new PriorityAlertDecorator(base, "CRITICAL");

        assertTrue(priority.getCondition().contains("Low Oxygen"));
        assertTrue(priority.getCondition().contains("Priority: CRITICAL"));
    }

    @Test
    void testPriorityAlertDecorator_preservesPatientId() {
        Alert base = new Alert("2", "Low Oxygen", 2000L);
        Alert priority = new PriorityAlertDecorator(base, "HIGH");

        assertEquals("2", priority.getPatientId());
        assertEquals(2000L, priority.getTimestamp());
    }

    @Test
    void testStackedDecorators_bothApplied() {
        // wrapping a repeated alert with a priority decorator
        Alert base = new Alert("3", "ECG Spike", 3000L);
        Alert repeated = new RepeatedAlertDecorator(base, 2);
        Alert priority = new PriorityAlertDecorator(repeated, "HIGH");

        assertTrue(priority.getCondition().contains("ECG Spike"));
        assertTrue(priority.getCondition().contains("Repeated 2 time(s)"));
        assertTrue(priority.getCondition().contains("Priority: HIGH"));
    }
}
