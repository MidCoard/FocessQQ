package com.focess.api.event.server;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;

/**
 * Called when MainPlugin is loaded completely
 */
public class ServerStartEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
}
