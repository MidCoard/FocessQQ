package top.focess.qq.api.event.bot;

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
    private Message message;

    /**
     * The target contact
     */
    private final Contact target;
    private boolean needUpdate;

    /**
     * Constructs a BotPreSendMessageEvent
     *
     * @param b       the bot
     * @param message the message
     * @param target  the target contact
     */
    public BotPreSendMessageEvent(final Bot b, final Message message, final Contact target) {
        super(b);
        this.message = message;
        this.target = target;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(final Message message) {
        if (this.message != message) {
            this.message = message;
            this.needUpdate = true;
        }
    }

    public Contact getTarget() {
        return this.target;
    }

    public boolean isNeedUpdate() {
        return this.needUpdate;
    }
}
