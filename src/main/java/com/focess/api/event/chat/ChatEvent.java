package com.focess.api.event.chat;

import com.focess.api.event.Event;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;

public abstract class ChatEvent extends Event {

    private final MessageChain message;

    public ChatEvent(MessageChain message) {
        this.message = message;
    }

    public MessageChain getMessage() {
        return message;
    }
}
