package com.focess.api.event.bot;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;

/**
 * Called when bot login
 */
public class BotLoginEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Constructs a BotLoginEvent
     *
     * @param bot the bot
     */
    public BotLoginEvent(Bot bot) {
        super(bot);
    }
}
