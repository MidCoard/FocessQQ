package com.focess.api.event;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.MessageChain;

public class FriendChatEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    private final Friend friend;
    private final MessageChain message;

    public FriendChatEvent(Friend friend, MessageChain message) {

        this.friend = friend;
        this.message = message;
    }

    public Friend getFriend() {
        return friend;
    }

    public MessageChain getMessage() {
        return message;
    }
}
