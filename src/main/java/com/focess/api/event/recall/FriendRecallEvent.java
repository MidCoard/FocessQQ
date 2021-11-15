package com.focess.api.event.recall;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;
import com.focess.api.event.bot.BotEvent;
import net.mamoe.mirai.contact.Friend;

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
     * @param bot the bot
     * @param friend the friend who send the message
     * @param messageIds the message ids
     */
    public FriendRecallEvent(Bot bot, Friend friend, int[] messageIds) {
        super(bot);
        this.friend = friend;
        this.messageIds = messageIds;
    }

    public Friend getFriend() {
        return friend;
    }

    public int[] getMessageIds() {
        return messageIds;
    }
}
