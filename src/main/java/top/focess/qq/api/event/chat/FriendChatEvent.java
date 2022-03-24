package top.focess.qq.api.event.chat;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.bot.message.MessageSource;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when a friend chat with bot
 */
public class FriendChatEvent extends ChatEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The friend who chats with bot
     */
    private final Friend friend;

    /**
     * The source of the message
     */
    private final MessageSource source;

    /**
     * Constructs a FriendChatEvent
     *  @param bot the bot
     * @param friend the friend who chats with bot
     * @param message the chat message
     * @param source the source of the message
     */
    public FriendChatEvent(Bot bot, Friend friend, MessageChain message, MessageSource source) {
        super(bot,message, source);
        this.friend = friend;
        this.source = source;
    }

    public Friend getFriend() {
        return friend;
    }

    public MessageSource getSource() {
        return source;
    }
}
