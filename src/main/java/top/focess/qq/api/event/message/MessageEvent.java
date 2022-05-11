package top.focess.qq.api.event.message;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.bot.message.MessageSource;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;

/**
 * Called when a friend chat with bot or a member chat in a group the bot is in (this does not execute any commands)
 */
public class MessageEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The chat message
     */
    private final Message message;

    /**
     * The source of the message
     */
    private final MessageSource source;

    /**
     * Constructs a MessageEvent
     *
     * @param bot     the bot
     * @param message the chat message
     * @param source  the source of the message
     */
    public MessageEvent(final Bot bot, final Message message, final MessageSource source) {
        super(bot);
        this.message = message;
        this.source = source;
    }

    public Message getMessage() {
        return this.message;
    }

    public MessageSource getSource() {
        return this.source;
    }
}
