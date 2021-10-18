package com.focess.api.event;

import org.jetbrains.annotations.NotNull;

/**
 * Called when Console input a String
 */
public class ConsoleInputEvent extends Event{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The console input
     */
    private final String input;

    /**
     * Constructs a ConsoleInputEvent
     *
     * @param input the console input
     */
    public ConsoleInputEvent(String input) {
        this.input = input;
    }

    @NotNull
    public String getInput() {
        return input;
    }
}
