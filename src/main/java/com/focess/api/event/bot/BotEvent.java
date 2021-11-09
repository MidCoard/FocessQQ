package com.focess.api.event.bot;

import com.focess.api.bot.Bot;
import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;

/**
 * Called when an event is relative with a bot
 */
public class BotEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The bot
     */
    private final Bot bot;

    /**
     * Constructs a BotEvent
     *
     * @param bot the bot
     */
    public BotEvent(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }
}
