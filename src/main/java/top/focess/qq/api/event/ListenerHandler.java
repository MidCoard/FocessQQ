package top.focess.qq.api.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;
import top.focess.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to help invoke listener methods
 */
@PermissionEnv(values = {Permission.REGISTER_LISTENER, Permission.REMOVE_LISTENER})
public class ListenerHandler {

    static final Map<Listener, Plugin> LISTENER_PLUGIN_MAP = Maps.newConcurrentMap();
    //only access in classloader, classloader is in lock process
    private static final List<ListenerHandler> LISTENER_HANDLER_LIST = Lists.newArrayList();
    private static final Map<Plugin, List<Listener>> PLUGIN_LISTENER_MAP = Maps.newConcurrentMap();
    //listeners only in one ListenerHandler, and one ListenerHandler is only accessed by synchronized

    protected final List<Pair<Pair<Listener,Method>, EventHandler>> listeners = Lists.newCopyOnWriteArrayList();

    public ListenerHandler() {
        LISTENER_HANDLER_LIST.add(this);
    }

    /**
     * Note: this is for test only
     * @return the listener handler list
     */
    public int size() {
        return this.listeners.size();
    }

    /**
     * Unregister all listeners bundled to the plugin
     *
     * @param plugin the plugin which need to unregister all its listeners
     */
    public static void unregister(final Plugin plugin) {
        Permission.checkPermission(Permission.REMOVE_LISTENER);
        final List<Listener> listeners = PLUGIN_LISTENER_MAP.getOrDefault(plugin, Lists.newArrayList());
        for (final ListenerHandler handler : LISTENER_HANDLER_LIST)
            for (final Listener listener : listeners) {
                LISTENER_PLUGIN_MAP.remove(listener);
                handler.unregister(listener);
            }
        PLUGIN_LISTENER_MAP.remove(plugin);
    }

    /**
     * Unregister the listener by the plugin
     *
     * @param plugin the plugin of the listener
     * @param listener the listener need to be unregistered
     */
    public static void unregister(final Plugin plugin, final Listener listener) {
        Permission.checkPermission(Permission.REMOVE_LISTENER);
        PLUGIN_LISTENER_MAP.computeIfPresent(plugin, (k, v) -> {
            v.remove(listener);
            return v;
        });
        for (final ListenerHandler handler : LISTENER_HANDLER_LIST)
            handler.unregister(listener);
        LISTENER_PLUGIN_MAP.remove(listener);
    }

    /**
     * Add the listener and bundle to the plugin
     *
     * @param plugin   the plugin
     * @param listener the listener
     */
    public static void register(final Plugin plugin, final Listener listener) {
        Permission.checkPermission(Permission.REGISTER_LISTENER);
        PLUGIN_LISTENER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(listener);
            return v;
        });
        LISTENER_PLUGIN_MAP.put(listener, plugin);
        for (final Method method : listener.getClass().getDeclaredMethods()) {
            final EventHandler handler;
            if ((handler = method.getAnnotation(EventHandler.class)) != null) {
                if (method.getParameterTypes().length == 1) {
                    final Class<?> eventClass = method.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(eventClass) && !Modifier.isAbstract(eventClass.getModifiers())) {
                        try {
                            final Field field = eventClass.getDeclaredField("LISTENER_HANDLER");
                            final boolean flag = field.canAccess(null);
                            field.setAccessible(true);
                            final ListenerHandler listenerHandler = (ListenerHandler) field.get(null);
                            field.setAccessible(flag);
                            listenerHandler.register(listener, method, handler);
                        } catch (final Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Unregister all listeners
     *
     * @return true if there are some listeners not belonging to MainPlugin not been unregistered, false otherwise
     */
    public static boolean unregisterAll() {
        Permission.checkPermission(Permission.REMOVE_LISTENER);
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
        Permission.checkPermission(Permission.REMOVE_LISTENER);
        this.listeners.removeIf(i -> i.getKey().getKey().equals(listener));
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
        this.listeners.add(Pair.of(Pair.of(listener,method), handler));
        this.listeners.sort(Comparator.comparing(pair -> pair.getValue().priority().getPriority()));
    }

    /**
     * Submit the event to this listener handler
     *
     * @param event the event need to be submitted
     * @param <T>   the event type
     */
    public <T extends Event> void submit(final T event) {
        this.listeners.forEach(
            i -> {
                if (event.isPrevent() && i.getValue().notCallIfPrevented()) {
                    FocessQQ.getLogger().debugLang("debug-prevent-event", event.toString(),  i.toString());
                    return;
                }
                if (event instanceof Cancellable && ((Cancellable) event).isCancelled() && i.getValue().notCallIfCancelled()) {
                    FocessQQ.getLogger().debugLang("debug-cancel-event", event.toString(),  i.toString());
                    return;
                }
                FocessQQ.getLogger().debugLang("debug-submit-event", event.toString(),  i.toString());
                final Method method = i.getKey().getValue();
                try {
                    method.setAccessible(true);
                    method.invoke(i.getKey().getKey(), event);
                } catch (final Exception e) {
                    FocessQQ.getLogger().thrLang("exception-handle-event", e, event.getClass().getName());
                }
            }
        );
    }
}
