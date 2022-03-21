package top.focess.qq.api.event.bot;

import net.mamoe.mirai.event.events.MessagePreSendEvent;
import net.mamoe.mirai.message.data.Message;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Contact;
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
     * @param b the bot
     * @param message the message
     * @param target the target contact
     * @param event the raw event
     */
    public BotPreSendMessageEvent(Bot b, Message message, Contact target, MessagePreSendEvent event) {
        super(b);
        this.message = message;
        this.target = target;
        this.event = event;
    }

    public Message getMessage() {
        return message;
    }

    public Contact getTarget() {
        return target;
    }

    public void setMessage(Message message) {
        this.event.setMessage(message);
    }
}
