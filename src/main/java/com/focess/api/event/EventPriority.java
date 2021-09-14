package com.focess.api.event;

public enum EventPriority {

    LOWEST(6), LOWER(5), LOW(4), NORMAL(3), HIGH(2), HIGHER(1), HIGHEST(0);

    private final int priority;

    EventPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}
