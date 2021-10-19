package com.focess.api.event.message;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * Called when a friend chat with bot or a member chat in a group the bot is in (this does not execute any commands)
 */
public class MessageEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The chat message
     */
    private final MessageChain message;

    /**
     * Constructs a MessageEvent
     *
     * @param message the chat message
     */
    public MessageEvent(MessageChain message) {
        this.message = message;
    }

    public MessageChain getMessage() {
        return message;
    }
}
