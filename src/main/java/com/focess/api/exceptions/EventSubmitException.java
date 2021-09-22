package com.focess.api.exceptions;

import com.focess.api.event.Event;

public class EventSubmitException extends Exception {

    public EventSubmitException(Event event) {
        super("Event " + event.getClass().getSimpleName() + " cannot be submitted.");
    }

    public EventSubmitException(Event event, String message) {
        super("Event " + event.getClass().getSimpleName() + " cannot be submitted. Message: " + message);
    }
}
