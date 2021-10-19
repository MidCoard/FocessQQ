package com.focess.api.event.message;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;

/**
 * Called when Console input a String (this does not execute any commands)
 */
public class ConsoleMessageEvent extends Event {


    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The console message
     */
    private final String message;

    public ConsoleMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
