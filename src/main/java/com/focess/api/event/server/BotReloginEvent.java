package com.focess.api.event.server;

import com.focess.api.event.Cancellable;
import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;

/**
 * Called when bot is relogined
 */
public class BotReloginEvent extends Event implements Cancellable {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * Indicate this event is cancelled
     */
    private boolean cancelled;

    /**
     * Constructs a BotReloginEvent
     */
    public BotReloginEvent() {
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
