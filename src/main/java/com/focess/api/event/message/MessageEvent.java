package com.focess.api.event.message;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;
import com.focess.api.event.bot.BotEvent;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * Called when a friend chat with bot or a member chat in a group the bot is in (this does not execute any commands)
 */
public class MessageEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The chat message
     */
    private final MessageChain message;

    /**
     * Constructs a MessageEvent
     *
     * @param bot the bot
     * @param message the chat message
     */
    public MessageEvent(Bot bot, MessageChain message) {
        super(bot);
        this.message = message;
    }

    public MessageChain getMessage() {
        return message;
    }
}
