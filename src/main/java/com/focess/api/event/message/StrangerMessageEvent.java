package com.focess.api.event.message;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * Called when a stranger chat with bot (this does not execute any commands)
 */
public class StrangerMessageEvent extends MessageEvent{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The stranger who chats with bot
     */
    private final Stranger stranger;

    /**
     * Constructs a StrangerMessageEvent
     *
     * @param bot     the bot
     * @param message the chat message
     * @param stranger the stranger who chats with bot
     */
    public StrangerMessageEvent(Bot bot, MessageChain message, Stranger stranger) {
        super(bot, message);
        this.stranger = stranger;
    }

    public Stranger getStranger() {
        return stranger;
    }
}
