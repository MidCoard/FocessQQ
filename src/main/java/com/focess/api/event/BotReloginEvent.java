package com.focess.api.event;

public class BotReloginEvent extends Event implements Cancelable {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    private boolean isCancelled;

    public BotReloginEvent() {
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}
