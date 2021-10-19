package com.focess.api.event.chat;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * Called when a friend chat with bot or a member chat in a group the bot is in
 */
public class ChatEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The chat message
     */
    private final MessageChain message;

    /**
     * Constructs a ChatEvent
     *
     * @param message the chat message
     */
    public ChatEvent(MessageChain message) {
        this.message = message;
    }

    public MessageChain getMessage() {
        return message;
    }
}
