package top.focess.qq.api.event.bot;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Contact;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when bot send message
 */
public class BotSendMessageEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The message
     */
    private final Message message;

    /**
     * The target contact
     */
    private final Contact contract;

    /**
     * Constructs a BotEvent
     *
     * @param bot      the bot
     * @param message  the message
     * @param contract the target contact
     */
    public BotSendMessageEvent(final Bot bot, final Message message, final @Nullable Contact contract) {
        super(bot);
        this.message = message;
        this.contract = contract;
    }

    public Message getMessage() {
        return this.message;
    }

    public @Nullable Contact getContact() {
        return this.contract;
    }
}
