package com.focess.api.event;

import com.focess.api.exception.EventSubmitException;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class EventManager {

    private static final Map<Class<? extends Event>, ListenerHandler> LISTENER_HANDLER_MAP = Maps.newHashMap();

    public static <T extends Event> void submit(T event) throws EventSubmitException {
        if (!Modifier.isAbstract(event.getClass().getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(event.getClass())) == null) {
                try {
                    Field field = event.getClass().getDeclaredField("LISTENER_HANDLER");
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    listenerHandler = (ListenerHandler) field.get(null);
                    field.setAccessible(flag);
                    LISTENER_HANDLER_MAP.put(event.getClass(), listenerHandler);
                } catch (Exception e) {
                    throw new EventSubmitException(event, "This event doesn't contain a LISTENER_HANDLER field.");
                }
            }
            listenerHandler.submit(event);
        } else throw new EventSubmitException(event, "This event is an abstract class.");
    }
}
