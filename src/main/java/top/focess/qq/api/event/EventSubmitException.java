package top.focess.qq.api.event;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate there is any invalid section in Event Submit Chain
 */
public class EventSubmitException extends Exception {

    /**
     * Constructs a EventSubmitException
     *
     * @param event the event
     */
    public EventSubmitException(@NotNull final Event event) {
        super("Event " + event.getClass().getSimpleName() + " cannot be submitted.");
    }

    /**
     * Constructs a EventSubmitException
     *
     * @param event   the event
     * @param message the detail message
     */
    public EventSubmitException(@NotNull final Event event, final String message) {
        super("Event " + event.getClass().getSimpleName() + " cannot be submitted. Message: " + message);
    }
}
