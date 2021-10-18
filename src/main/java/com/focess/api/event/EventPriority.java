package com.focess.api.event;

/**
 * This class is used to define the priority of the event listener to better define the whole event chain
 */
public enum EventPriority {

    LOWEST(6), LOWER(5), LOW(4), NORMAL(3), HIGH(2), HIGHER(1), HIGHEST(0);

    /**
     * The priority internal value
     */
    private final int priority;

    EventPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}
