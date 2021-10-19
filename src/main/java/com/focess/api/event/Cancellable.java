package com.focess.api.event;

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
     * @param cancelled whether this event is cancelled or not
     */
    void setCancelled(boolean cancelled);
}
