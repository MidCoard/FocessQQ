package com.focess.api.event.bot;

import com.focess.api.bot.Bot;
import com.focess.api.event.Cancellable;
import com.focess.api.event.ListenerHandler;

/**
 * Called when bot is relogined
 */
public class BotReloginEvent extends BotEvent implements Cancellable {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Indicate this event is cancelled
     */
    private boolean cancelled;

    /**
     * Constructs a BotReloginEvent
     */
    public BotReloginEvent(Bot bot) {
        super(bot);
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
