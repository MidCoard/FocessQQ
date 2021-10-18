package com.focess.api.event;

import com.focess.Main;
import com.focess.api.exceptions.EventSubmitException;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * This class is used to submit Event for developers.
 */
public class EventManager {

    private static final Map<Class<? extends Event>, ListenerHandler> LISTENER_HANDLER_MAP = Maps.newHashMap();

    private static <T> T cast(Object t) {
        return (T) t;
    }

    /**
     * Submit the event to all of its implemented super event class
     *
     * @param event the event need to be submitted
     * @param <T> the event type
     * @throws EventSubmitException if class of this event is abstract or there is no LISTENER_HANDLER in this event
     */
    public static <T extends Event> void submit(T event) throws EventSubmitException {
        submit(cast(event.getClass()),event);
    }

    /**
     * Submit the event to cls and all of cls 's implemented super event class
     *
     * @param cls the submitting chain start event
     * @param event the event need to be submitted
     * @param <T> the event type
     * @throws EventSubmitException if class of this event is abstract or there is no LISTENER_HANDLER in this event
     */
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

    /**
     * Submit the event only to itself and no exception throws
     *
     * @param cls the submitting chain start event
     * @param event the event need to be submitted
     * @param <T> the event type
     */
    public static <T extends Event> void trySubmitOnce(Class<T> cls,T event){
        try {
            submitOnce(cls,event);
        } catch (EventSubmitException e) {
            Main.getLogger().trace("Try Submit Failed",e);
        }
    }

    /**
     * Submit the event only to cls event class
     *
     * @param cls the event class the event submits to
     * @param event the event need to be submitted
     * @param <T> the event type
     * @throws EventSubmitException if class of this event is abstract or there is no LISTENER_HANDLER in this event
     */
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
