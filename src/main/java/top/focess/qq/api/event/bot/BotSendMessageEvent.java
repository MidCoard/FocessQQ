package top.focess.qq.api.event.bot;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Contact;
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
     * @param bot the bot
     * @param message the message
     * @param contract the target contact
     */
    public BotSendMessageEvent(Bot bot, Message message, Contact contract) {
        super(bot);
        this.message = message;
        this.contract = contract;
    }

    public Message getMessage() {
        return message;
    }

    public Contact getContract() {
        return contract;
    }
}
