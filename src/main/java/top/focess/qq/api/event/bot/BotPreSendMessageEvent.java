package top.focess.qq.api.event.bot;

import net.mamoe.mirai.event.events.MessagePreSendEvent;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Contact;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when bot pre-send message
 */
public class BotPreSendMessageEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The pre-send message
     */
    private final Message message;

    /**
     * The target contact
     */
    private final Contact target;

    /**
     * The raw event
     */
    private final MessagePreSendEvent event;

    /**
     * Constructs a BotPreSendMessageEvent
     *
     * @param b       the bot
     * @param message the message
     * @param target  the target contact
     * @param event   the raw event
     */
    public BotPreSendMessageEvent(final Bot b, final Message message, final Contact target, final MessagePreSendEvent event) {
        super(b);
        this.message = message;
        this.target = target;
        this.event = event;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(final Message message) {
        this.event.setMessage(message.getNativeMessage());
    }

    public Contact getTarget() {
        return this.target;
    }
}
