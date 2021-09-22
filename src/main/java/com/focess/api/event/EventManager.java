package com.focess.api.event;

import com.focess.Main;
import com.focess.api.exceptions.EventSubmitException;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class EventManager {

    private static final Map<Class<? extends Event>, ListenerHandler> LISTENER_HANDLER_MAP = Maps.newHashMap();

    private static <T> T cast(Object t) {
        return (T) t;
    }

    public static <T extends Event> void submit(T event) throws EventSubmitException {
        submit(cast(event.getClass()),event);
    }

    public static <T extends Event> void submit(Class<T> cls,T event) throws EventSubmitException {
        if (!Modifier.isAbstract(cls.getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(cls)) == null) {
                try {
                    Field field = cls.getDeclaredField("LISTENER_HANDLER");
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    listenerHandler = (ListenerHandler) field.get(null);
                    field.setAccessible(flag);
                    LISTENER_HANDLER_MAP.put(cls, listenerHandler);
                } catch (Exception e) {
                    throw new EventSubmitException(event, "This event doesn't contain a LISTENER_HANDLER field.");
                }
            }
            listenerHandler.submit(event);
            Class<?> c = cls;
            while (!(c = c.getSuperclass()).equals(Event.class))
                trySubmitOnce(cast(c),event);
        } else throw new EventSubmitException(event, "This event is an abstract class.");
    }

    public static <T extends Event> void trySubmitOnce(Class<T> cls,T event){
        try {
            submitOnce(cls,event);
        } catch (EventSubmitException e) {
            Main.getLogger().trace("Try Submit Failed",e);
        }
    }

    public static <T extends Event> void submitOnce(Class<T> cls,T event) throws EventSubmitException {
        if (!Modifier.isAbstract(cls.getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(cls)) == null) {
                try {
                    Field field = cls.getDeclaredField("LISTENER_HANDLER");
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    listenerHandler = (ListenerHandler) field.get(null);
                    field.setAccessible(flag);
                    LISTENER_HANDLER_MAP.put(cls, listenerHandler);
                } catch (Exception e) {
                    throw new EventSubmitException(event, "This event doesn't contain a LISTENER_HANDLER field.");
                }
            }
            listenerHandler.submit(event);
        } else throw new EventSubmitException(event, "This event is an abstract class.");
    }

}
