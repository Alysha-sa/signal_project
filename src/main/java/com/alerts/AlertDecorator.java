package com.alerts;

/**
 * Base decorator class for alerts. It wraps an existing alert and passes
 * all calls through to it, so subclasses can add extra behaviour on top
 * without changing the original alert.
 */
public abstract class AlertDecorator extends Alert {

    // The alert being decorated.
    protected final Alert decoratedAlert;

    /**
     * Creates a decorator wrapping the given alert.
     *
     * @param decoratedAlert the alert to wrap
     */
    public AlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert.getPatientId(),
              decoratedAlert.getCondition(),
              decoratedAlert.getTimestamp());
        this.decoratedAlert = decoratedAlert;
    }

    /**
     * Returns the condition of the wrapped alert.
     *
     * @return the condition string
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition();
    }
}
