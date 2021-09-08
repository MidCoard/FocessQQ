package com.focess.api.event;

import com.focess.Main;
import com.focess.api.Plugin;
import com.focess.api.annotation.EventHandler;
import com.focess.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ListenerHandler {

    private static final List<ListenerHandler> LISTENER_HANDLER_LIST = Lists.newArrayList();
    private static final Map<Plugin, List<Listener>> PLUGIN_LISTENER_MAP = Maps.newHashMap();
    private final Map<Listener, List<Pair<Method, EventHandler>>> listeners = Maps.newHashMap();

    public ListenerHandler() {
        LISTENER_HANDLER_LIST.add(this);
    }

    public static void unregisterPlugin(Plugin plugin) {
        for (ListenerHandler handler : LISTENER_HANDLER_LIST)
            for (Listener listener : PLUGIN_LISTENER_MAP.getOrDefault(plugin, Lists.newArrayList()))
                handler.unregisterListener(listener);
    }

    public static void addListener(Plugin plugin, Listener listener) {
        PLUGIN_LISTENER_MAP.compute(plugin, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(listener);
            return v;
        });
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public <T extends Event> void addListener(Listener listener, Method method, EventHandler handler) {
        listeners.compute(listener, (k, v) -> {
            if (v == null)
                v = Lists.newArrayList();
            v.add(Pair.of(method, handler));
            return v;
        });
    }

    public <T extends Event> void submit(T event) {
        for (Listener listener : this.listeners.keySet()) {
            this.listeners.get(listener).stream().sorted(Comparator.comparing(pair -> pair.getValue().priority())).forEachOrdered(
                    i -> {
                        if (event instanceof Cancelable && ((Cancelable) event).isCancelled() && i.getValue().notCallIfCancelled())
                            return;
                        Method method = i.getKey();
                        try {
                            boolean flag = method.isAccessible();
                            method.setAccessible(true);
                            method.invoke(listener, event);
                            method.setAccessible(flag);
                        } catch (Exception e) {
                            Main.getLogger().thr("Invoke Event Exception",e);
                        }
                    }
            );
        }
    }
}
