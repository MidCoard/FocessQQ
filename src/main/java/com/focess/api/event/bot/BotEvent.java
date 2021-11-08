package com.focess.api.event.bot;

import com.focess.api.bot.Bot;
import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;

public class BotEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    private final Bot bot;

    public BotEvent(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }
}
