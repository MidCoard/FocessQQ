package top.focess.qq.api.event.chat;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;

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
    private final OnlineMessageSource.Incoming.FromFriend source;

    /**
     * Constructs a FriendChatEvent
     *  @param bot the bot
     * @param friend the friend who chats with bot
     * @param message the chat message
     * @param source the source of the message
     */
    public FriendChatEvent(Bot bot, Friend friend, MessageChain message, OnlineMessageSource.Incoming.FromFriend source) {
        super(bot,message);
        this.friend = friend;
        this.source = source;
    }

    public Friend getFriend() {
        return friend;
    }

    public OnlineMessageSource.Incoming.FromFriend getSource() {
        return source;
    }
}
