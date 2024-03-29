package top.focess.qq.api.event;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.scheduler.Schedulers;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;
import top.focess.scheduler.Scheduler;
import top.focess.scheduler.Task;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is used to submit Event for developers.
 */
@PermissionEnv(values = Permission.EVENT_SUBMIT)
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
        Permission.checkPermission(Permission.EVENT_SUBMIT);
        final Task task = SCHEDULER.run(() -> {
            try {
                submit(cast(event.getClass()), event);
            } catch (final EventSubmitException e) {
                throw new EventSubmitRuntimeException(e);
            }
        }, "submit-" + event.getClass().getName());
        try {
            task.join(10, TimeUnit.SECONDS);
        } catch (final ExecutionException | InterruptedException | CancellationException | TimeoutException e) {
            if (e.getCause() instanceof EventSubmitRuntimeException)
                throw (EventSubmitException) e.getCause().getCause();
            else FocessQQ.getLogger().thrLang("exception-submit-event",e);
        }
    }

    private static <T extends Event> void submit(@NotNull final Class<T> cls, final T event) throws EventSubmitException {
        if (!Modifier.isAbstract(cls.getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(cls)) == null) {
                try {
                    final Field field = cls.getDeclaredField("LISTENER_HANDLER");
                    final boolean flag = field.canAccess(null);
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

    private static <T extends Event> void trySubmitOnce(final Class<T> cls, final T event) throws EventSubmitException {
        submitOnce(cls, event);
    }

    private static <T extends Event> void submitOnce(@NotNull final Class<T> cls, final T event) throws EventSubmitException {
        if (!Modifier.isAbstract(cls.getModifiers())) {
            ListenerHandler listenerHandler;
            if ((listenerHandler = LISTENER_HANDLER_MAP.get(cls)) == null) {
                try {
                    final Field field = cls.getDeclaredField("LISTENER_HANDLER");
                    final boolean flag = field.canAccess(null);
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
