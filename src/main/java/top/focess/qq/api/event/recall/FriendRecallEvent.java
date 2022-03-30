package top.focess.qq.api.event.recall;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;

/**
 * Called when a Friend recall a message
 */
public class FriendRecallEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The friend who send the message
     */
    private final Friend friend;

    /**
     * The message ids
     */
    private final int[] messageIds;

    /**
     * Constructs a FriendRecallEvent
     *
     * @param bot        the bot
     * @param friend     the friend who send the message
     * @param messageIds the message ids
     */
    public FriendRecallEvent(final Bot bot, final Friend friend, final int[] messageIds) {
        super(bot);
        this.friend = friend;
        this.messageIds = messageIds;
    }

    public Friend getFriend() {
        return this.friend;
    }

    public int[] getMessageIds() {
        return this.messageIds;
    }
}
