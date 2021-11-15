package com.focess.api.event.bot;

import com.focess.api.bot.Bot;

/**
 * Called when bot logout
 */
public class BotLogoutEvent extends BotEvent {

    /**
     * Constructs a BotLogoutEvent
     *
     * @param bot the bot
     */
    public BotLogoutEvent(Bot bot) {
        super(bot);
    }
}
