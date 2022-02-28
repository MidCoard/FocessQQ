package top.focess.qq.api.event.message;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * Called when a friend chat with bot (this does not execute any commands)
 */
public class FriendMessageEvent extends MessageEvent{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The friend who chats with bot
     */
    private final Friend friend;

    /**
     * Constructs a FriendMessageEvent
     *
     * @param bot the bot
     * @param message the chat message
     * @param friend the friend who chats with bot
     */
    public FriendMessageEvent(Bot bot, Friend friend, MessageChain message) {
        super(bot,message);
        this.friend = friend;
    }

    public Friend getFriend() {
        return friend;
    }
}
