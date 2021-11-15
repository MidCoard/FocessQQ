package com.focess.api.event.bot;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;

/**
 * Called when bot relogin
 */
public class BotReloginEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Constructs a BotReloginEvent
     *
     * @param bot the bot
     */
    public BotReloginEvent(Bot bot) {
        super(bot);
    }
}
