package com.focess.api.event.chat;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;

/**
 * Called when a stranger chat with bot
 */
public class StrangerChatEvent extends ChatEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The stranger who chats with bot
     */
    private final Stranger stranger;

    /**
     * The source of the message
     */
    private final OnlineMessageSource.Incoming.FromStranger source;

    /**
     * Constructs a ChatEvent
     *
     * @param bot     the bot
     * @param message the chat message
     * @param stranger the stranger who chats with bot
     * @param source the source of the message
     */
    public StrangerChatEvent(Bot bot, MessageChain message, Stranger stranger, OnlineMessageSource.Incoming.FromStranger source) {
        super(bot, message);
        this.stranger = stranger;
        this.source = source;
    }

    public Stranger getStranger() {
        return stranger;
    }

    public OnlineMessageSource.Incoming.FromStranger getSource() {
        return source;
    }
}
