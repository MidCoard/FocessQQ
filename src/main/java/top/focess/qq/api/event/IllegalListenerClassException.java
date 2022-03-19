package top.focess.qq.api.event;
/**
 * Thrown to indicate this class is an illegal Listener class
 */
public class IllegalListenerClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalListenerClassException
     *
     * @param c the illegal listener class
     */
    public IllegalListenerClassException(Class<?> c) {
        super("The class " + c.getName() + " is an illegal Listener class");
    }

    /**
     * Constructs a IllegalListenerClassException
     * @param c the illegal Listener class
     * @param e the cause
     */
    public IllegalListenerClassException(Class<? extends Listener> c, Exception e) {
        super("The class " + c.getName() + " is an illegal Listener class", e);
    }
}
