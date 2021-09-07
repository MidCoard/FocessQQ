package com.focess.api.event;

public enum EventPriority {

    LOWEST(0), LOWER(1), LOW(2), NORMAL(3), HIGH(4), HIGHER(5), HIGHEST(6);
`
    private final int priority;

    EventPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}
