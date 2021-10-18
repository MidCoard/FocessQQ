package com.focess.api.event.chat;

import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.MessageChain;

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
     * Constructs a FriendChatEvent
     *
     * @param friend the friend who chats with bot
     * @param message the chat message
     */
    public FriendChatEvent(Friend friend, MessageChain message) {
        super(message);
        this.friend = friend;
    }

    public Friend getFriend() {
        return friend;
    }
}
