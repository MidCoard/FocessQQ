package top.focess.qq.api.event.server;

import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when MainPlugin is starting unloading itself
 */
public class ServerStopEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
}
