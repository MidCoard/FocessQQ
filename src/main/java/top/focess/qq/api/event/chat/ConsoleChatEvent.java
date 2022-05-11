package top.focess.qq.api.event.chat;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.message.Message;
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
    private final Message message;

    /**
     * Constructs a ConsoleInputEvent
     *
     * @param message the console message
     */
    public ConsoleChatEvent(final Message message) {
        this.message = message;
    }

    @NotNull
    public Message getMessage() {
        return this.message;
    }
}
