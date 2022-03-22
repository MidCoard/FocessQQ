package top.focess.qq.api.event.chat;

import net.mamoe.mirai.message.data.OnlineMessageSource;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Stranger;
import top.focess.qq.api.bot.message.MessageChain;
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
     * The source of the message
     */
    private final OnlineMessageSource.Incoming.FromStranger source;

    /**
     * Constructs a ChatEvent
     *  @param bot     the bot
     * @param stranger the stranger who chats with bot
     * @param message the chat message
     * @param source the source of the message
     */
    public StrangerChatEvent(Bot bot,Stranger stranger, MessageChain message, OnlineMessageSource.Incoming.FromStranger source) {
        super(bot, message);
        this.stranger = stranger;
        this.source = source;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public OnlineMessageSource.Incoming.FromStranger getSource() {
        return source;
    }
}
