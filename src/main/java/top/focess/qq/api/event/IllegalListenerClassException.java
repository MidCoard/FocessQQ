package top.focess.qq.api.event;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate this class is an illegal Listener class
 */
public class IllegalListenerClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalListenerClassException
     *
     * @param c the illegal listener class
     */
    public IllegalListenerClassException(@NotNull final Class<?> c) {
        super("The class " + c.getName() + " is an illegal Listener class");
    }

    /**
     * Constructs a IllegalListenerClassException
     *
     * @param c the illegal Listener class
     * @param e the cause
     */
    public IllegalListenerClassException(@NotNull final Class<? extends Listener> c, final Exception e) {
        super("The class " + c.getName() + " is an illegal Listener class", e);
    }
}
