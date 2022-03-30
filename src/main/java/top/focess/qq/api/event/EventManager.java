package top.focess.qq.api.event;

import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.api.schedule.Task;
import top.focess.qq.core.debug.Section;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * This class is used to submit Event for developers.
 */
public class EventManager {

    private static final Scheduler SCHEDULER = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(), 10, false, "EventManager");

    private static final Map<Class<? extends Event>, ListenerHandler> LISTENER_HANDLER_MAP = Maps.newHashMap();

    private static <T> T cast(final Object t) {
        return (T) t;
    }

    /**
     * Submit the event to all of its implemented super event class
     *
     * @param event the event need to be submitted
     * @param <T>   the event type
     * @throws EventSubmitException if class of this event is abstract or there is no LISTENER_HANDLER in this event
     */
    public static <T extends Event> void submit(final T event) throws EventSubmitException {
        final Task task = SCHEDULER.run(() -> {
            try {
                submit(cast(event.getClass()), event);
            } catch (final EventSubmitException e) {
                throw new EventSubmitRuntimeException(e);
            }
        });
        final Section section = Section.startSection("event-submit", task, Duration.ofSeconds(10));
        try {
            task.join();
        } catch (final ExecutionException | InterruptedException | CancellationException e) {
            if (e.getCause() instanceof EventSubmitRuntimeException)
                throw (EventSubmitException) e.getCause().getCause();
            else FocessQQ.getLogger().debugLang("section-exception", section.getName(), e.getMessage());
        }
        section.stop();
    }

    /**
     * Submit the event to cls and all of cls 's implemented super event class
     *
     * @param cls   the submitting chain start event
     * @param event the event need to be submitted
     * @param <T>   the event type
     * @throws EventSubmitException if class of this event is abstract or there is no LISTENER_HANDLER in this event
     */
    private static <T extends Event> void submit(final Class<T> cls, final T event) throws EventSubmitException {
        if (!Modifier.isAbstract(cls.getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(cls)) == null) {
                try {
                    final Field field = cls.getDeclaredField("LISTENER_HANDLER");
                    final boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    listenerHandler = (ListenerHandler) field.get(null);
                    field.setAccessible(flag);
                    LISTENER_HANDLER_MAP.put(cls, listenerHandler);
                } catch (final Exception e) {
                    throw new EventSubmitException(event, "This event doesn't contain a LISTENER_HANDLER field.");
                }
            }
            listenerHandler.submit(event);
            Class<?> c = cls;
            while (!(c = c.getSuperclass()).equals(Event.class))
                trySubmitOnce(cast(c), event);
        } else throw new EventSubmitException(event, "This event is an abstract class.");
    }

    /**
     * Submit the event only to itself and no exception throws
     *
     * @param cls   the submitting chain start event
     * @param event the event need to be submitted
     * @param <T>   the event type
     */
    private static <T extends Event> void trySubmitOnce(final Class<T> cls, final T event) throws EventSubmitException {
        submitOnce(cls, event);
    }

    /**
     * Submit the event only to cls event class
     *
     * @param cls   the event class the event submits to
     * @param event the event need to be submitted
     * @param <T>   the event type
     * @throws EventSubmitException if class of this event is abstract or there is no LISTENER_HANDLER in this event
     */
    private static <T extends Event> void submitOnce(final Class<T> cls, final T event) throws EventSubmitException {
        if (!Modifier.isAbstract(cls.getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(cls)) == null) {
                try {
                    final Field field = cls.getDeclaredField("LISTENER_HANDLER");
                    final boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    listenerHandler = (ListenerHandler) field.get(null);
                    field.setAccessible(flag);
                    LISTENER_HANDLER_MAP.put(cls, listenerHandler);
                } catch (final Exception e) {
                    throw new EventSubmitException(event, "This event doesn't contain a LISTENER_HANDLER field.");
                }
            }
            listenerHandler.submit(event);
        } else throw new EventSubmitException(event, "This event is an abstract class.");
    }

    private static class EventSubmitRuntimeException extends RuntimeException {

        public EventSubmitRuntimeException(final EventSubmitException e) {
            super(e);
        }
    }

}
