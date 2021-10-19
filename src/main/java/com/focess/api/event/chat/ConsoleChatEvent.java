package com.focess.api.event.chat;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Called when Console input a String
 */
public class ConsoleChatEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The console message
     */
    private final String message;

    /**
     * Constructs a ConsoleInputEvent
     *
     * @param message the console message
     */
    public ConsoleChatEvent(String message) {
        this.message = message;
    }

    @NotNull
    public String getMessage() {
        return message;
    }
}
