package top.focess.qq.api.event;

/**
 * This class indicates that the implemented event class is cancellable
 */
public interface Cancellable {

    /**
     * Indicate this event is cancelled
     *
     * @return true if this event is cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     * Set whether this event is cancelled or not
     *
     * @param cancelled true if cancel this event, false not cancel this event
     */
    void setCancelled(boolean cancelled);
}
