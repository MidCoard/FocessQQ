package top.focess.qq.api.event;

import java.io.Serializable;

/**
 * This is the base class of Event Chain.
 * The class which extends it should have a field called "LISTENER_HANDLER"
 */
public abstract class Event implements Serializable {

    /**
     * Used to prevent submitting in the future Event Chain
     */
    private boolean prevent = false;

    public void setPrevent(boolean prevent) {
        this.prevent = prevent;
    }

    public boolean isPrevent() {
        return prevent;
    }
}
