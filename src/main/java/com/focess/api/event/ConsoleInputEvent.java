package com.focess.api.event;

/**
 *
 */
public class ConsoleInputEvent extends Event{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    private final String input;

    public ConsoleInputEvent(String input) {
        this.input = input;
    }

    /**
     * Get the console input String
     *
     * @return the console input String
     */
    public String getInput() {
        return input;
    }
}
