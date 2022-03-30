package top.focess.qq.api.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.Pair;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to help invoke listener methods
 */
public class ListenerHandler {

    static final Map<Listener, Plugin> LISTENER_PLUGIN_MAP = Maps.newConcurrentMap();
    //only access in classloader, classloader is in lock process
    private static final List<ListenerHandler> LISTENER_HANDLER_LIST = Lists.newArrayList();
    private static final Map<Plugin, List<Listener>> PLUGIN_LISTENER_MAP = Maps.newConcurrentMap();
    //listeners only in one ListenerHandler, and one ListenerHandler only access by synchronized
    private final Map<Listener, List<Pair<Method, EventHandler>>> listeners = Maps.newHashMap();

    public ListenerHandler() {
        LISTENER_HANDLER_LIST.add(this);
    }

    /**
     * Unregister all listeners bundle to the plugin
     *
     * @param plugin the plugin which need to unregister all its listeners
     */
    public static void unregister(final Plugin plugin) {
        final List<Listener> listeners = PLUGIN_LISTENER_MAP.getOrDefault(plugin, Lists.newArrayList());
        for (final ListenerHandler handler : LISTENER_HANDLER_LIST)
            for (final Listener listener : listeners) {
                LISTENER_PLUGIN_MAP.remove(listener);
                handler.unregister(listener);
            }
        PLUGIN_LISTENER_MAP.remove(plugin);
    }

    /**
     * Add the listener and bundle to the plugin
     *
     * @param plugin   the plugin
     * @param listener the listener
     */
    public static void register(final Plugin plugin, final Listener listener) {
        PLUGIN_LISTENER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(listener);
            return v;
        });
        LISTENER_PLUGIN_MAP.put(listener, plugin);
    }

    /**
     * Unregister all listeners
     *
     * @return true if there are some listeners not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        boolean ret = false;
        for (final Plugin plugin : PLUGIN_LISTENER_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                ret = true;
            unregister(plugin);
        }
        PLUGIN_LISTENER_MAP.clear();
        return ret;
    }

    /**
     * Unregister the listener
     *
     * @param listener the listener need to be unregistered
     */
    public void unregister(final Listener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Register the listener
     *
     * @param listener the listener
     * @param method   the listener method to this Event listener handler
     * @param handler  the event handler
     * @param <T>      the event type
     */
    public <T extends Event> void register(final Listener listener, final Method method, final EventHandler handler) {
        this.listeners.compute(listener, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(Pair.of(method, handler));
            return v;
        });
    }

    /**
     * Submit the event to this listener handler
     *
     * @param event the event need to be submitted
     * @param <T>   the event type
     */
    public <T extends Event> void submit(final T event) {
        for (final Listener listener : this.listeners.keySet()) {
            this.listeners.get(listener).stream().sorted(Comparator.comparing(pair -> pair.getValue().priority().getPriority())).forEachOrdered(
                    i -> {
                        if (event.isPrevent() && i.getValue().notCallIfPrevented())
                            return;
                        if (event instanceof Cancellable && ((Cancellable) event).isCancelled() && i.getValue().notCallIfCancelled())
                            return;
                        final Method method = i.getKey();
                        try {
                            method.setAccessible(true);
                            method.invoke(listener, event);
                        } catch (final Exception e) {
                            FocessQQ.getLogger().thrLang("exception-handle-event", e);
                        }
                    }
            );
        }
    }
}
