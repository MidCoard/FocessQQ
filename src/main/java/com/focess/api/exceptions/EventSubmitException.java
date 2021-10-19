package com.focess.api.exceptions;

import com.focess.api.event.Event;

/**
 * Thrown to indicate there is any invalid section in Event Submit Chain
 */
public class EventSubmitException extends Exception {

    /**
     * Constructs a EventSubmitException
     * @param event the event
     */
    public EventSubmitException(Event event) {
        super("Event " + event.getClass().getSimpleName() + " cannot be submitted.");
    }

    /**
     * Constructs a EventSubmitException
     *
     * @param event the event
     * @param message the detail message
     */
    public EventSubmitException(Event event, String message) {
        super("Event " + event.getClass().getSimpleName() + " cannot be submitted. Message: " + message);
    }
}
