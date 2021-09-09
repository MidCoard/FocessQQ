package com.focess.api.event.chat;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.MessageChain;

public class FriendChatEvent extends ChatEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    private final Friend friend;

    public FriendChatEvent(Friend friend, MessageChain message) {
        super(message);
        this.friend = friend;
    }

    public Friend getFriend() {
        return friend;
    }
}
