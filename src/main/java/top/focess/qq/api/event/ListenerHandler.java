package top.focess.qq.api.event;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to help invoke listener methods
 */
public class ListenerHandler {

    //only access in classloader, classloader is in lock process
    private static final List<ListenerHandler> LISTENER_HANDLER_LIST = Lists.newArrayList();
    private static final Map<Plugin, List<Listener>> PLUGIN_LISTENER_MAP = Maps.newConcurrentMap();
    static final Map<Listener,Plugin> LISTENER_PLUGIN_MAP = Maps.newConcurrentMap();

    //listeners only in one ListenerHandler, and one ListenerHandler only access by synchronized
    private final Map<Listener, List<Pair<Method, EventHandler>>> listeners = Maps.newHashMap();

    public ListenerHandler() {
        LISTENER_HANDLER_LIST.add(this);
    }

    /**
     *  Unregister all listeners bundle to the plugin
     *
     * @param plugin the plugin which need to unregister all its listeners
     */
    public static void unregister(Plugin plugin) {
        List<Listener> listeners = PLUGIN_LISTENER_MAP.getOrDefault(plugin,Lists.newArrayList());
        for (ListenerHandler handler : LISTENER_HANDLER_LIST)
            for (Listener listener : listeners) {
                LISTENER_PLUGIN_MAP.remove(listener);
                handler.unregister(listener);
            }
        PLUGIN_LISTENER_MAP.remove(plugin);
    }

    /**
     * Add the listener and bundle to the plugin
     *
     * @param plugin the plugin
     * @param listener the listener
     */
    public static void register(Plugin plugin, Listener listener) {
        PLUGIN_LISTENER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(listener);
            return v;
        });
        LISTENER_PLUGIN_MAP.put(listener,plugin);
    }

    /**
     * Unregister the listener
     *
     * @param listener the listener need to be unregistered
     */
    public void unregister(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Unregister all listeners
     *
     * @return true if there are some listeners not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        boolean ret = false;
        for (Plugin plugin : PLUGIN_LISTENER_MAP.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                ret = true;
            unregister(plugin);
        }
        PLUGIN_LISTENER_MAP.clear();
        return ret;
    }



    /**
     * Register the listener
     *
     * @param listener the listener
     * @param method the listener method to this Event listener handler
     * @param handler the event handler
     * @param <T> the event type
     */
    public <T extends Event> void register(Listener listener, Method method, EventHandler handler) {
        listeners.compute(listener, (k, v) -> {
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
     * @param <T> the event type
     */
    public <T extends Event> void submit(T event) {
        for (Listener listener : this.listeners.keySet()) {
            this.listeners.get(listener).stream().sorted(Comparator.comparing(pair -> pair.getValue().priority().getPriority())).forEachOrdered(
                    i -> {
                        if (event.isPrevent() && i.getValue().notCallIfPrevented())
                            return;
                        if (event instanceof Cancellable && ((Cancellable) event).isCancelled() && i.getValue().notCallIfCancelled())
                            return;
                        Method method = i.getKey();
                        try {
                            method.setAccessible(true);
                            method.invoke(listener, event);
                        } catch (Exception e) {
                            FocessQQ.getLogger().thrLang("exception-handle-event",e);
                        }
                    }
            );
        }
    }
}
