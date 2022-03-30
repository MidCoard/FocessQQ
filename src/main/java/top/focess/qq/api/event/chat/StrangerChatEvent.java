package top.focess.qq.api.event.chat;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Stranger;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.bot.message.MessageSource;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when a stranger chat with bot
 */
public class StrangerChatEvent extends ChatEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The stranger who chats with bot
     */
    private final Stranger stranger;

    /**
     * Constructs a ChatEvent
     *
     * @param bot      the bot
     * @param stranger the stranger who chats with bot
     * @param message  the chat message
     * @param source   the source of the message
     */
    public StrangerChatEvent(final Bot bot, final Stranger stranger, final MessageChain message, final MessageSource source) {
        super(bot, message, source);
        this.stranger = stranger;
    }

    public Stranger getStranger() {
        return this.stranger;
    }
}
