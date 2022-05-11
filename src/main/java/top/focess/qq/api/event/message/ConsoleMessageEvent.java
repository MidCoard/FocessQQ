package top.focess.qq.api.event.message;

import top.focess.qq.api.bot.message.TextMessage;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when Console input a String (this does not execute any commands)
 */
public class ConsoleMessageEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The console message
     */
    private final TextMessage message;

    public ConsoleMessageEvent(final TextMessage message) {
        this.message = message;
    }

    public TextMessage getMessage() {
        return this.message;
    }
}
