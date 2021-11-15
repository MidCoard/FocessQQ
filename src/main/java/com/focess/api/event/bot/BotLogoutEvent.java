package com.focess.api.event.bot;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;

/**
 * Called when bot logout
 */
public class BotLogoutEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Constructs a BotLogoutEvent
     *
     * @param bot the bot
     */
    public BotLogoutEvent(Bot bot) {
        super(bot);
    }
}
