package top.focess.qq.api.event.chat;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;

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
    public ConsoleChatEvent(final String message) {
        this.message = message;
    }

    @NotNull
    public String getMessage() {
        return this.message;
    }
}
