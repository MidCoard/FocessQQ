package com.focess.api.event.bot;

import com.focess.api.bot.Bot;

/**
 * Called when bot login
 */
public class BotLoginEvent extends BotEvent {

    /**
     * Constructs a BotLoginEvent
     *
     * @param bot the bot
     */
    public BotLoginEvent(Bot bot) {
        super(bot);
    }
}
