package top.focess.qq.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represent an event listener method. It means this method mush own one argument whose class is an implemented Event.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    /**
     * Set the priority of this event listener method
     *
     * @return the priority of this event listener method
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Set whether this method should be called when the event is cancelled
     *
     * @return true if this method should be called when the event is cancelled, false otherwise
     */
    boolean notCallIfCancelled() default false;

    /**
     * Set whether this method should be called when the event is prevented
     *
     * @return true if this method should be called when the event is prevented, false otherwise
     */
    boolean notCallIfPrevented() default false;
}
