package com.alerts;

/**
 * A decorator that labels alert as repeated, showing that the
 * condition is still there, so the alert has to be checked and/or sent
 * again.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final int repeatCount;

    /**
     * Creates a repeated alert decorator for the given alert.
     *
     * @param decoratedAlert the alert to wrap
     * @param repeatCount how many times the alert has been repeated
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, int repeatCount) {
        super(decoratedAlert);
        this.repeatCount = repeatCount;
    }

    /**
     * Returns the condition of the original alert with the amount of times
     * the this alert has been triggered.
     *
     * @return the condition string with repeat information
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " [Repeated " + repeatCount + " time(s)]";
    }
}
