package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate this port is not available
 */
public class IllegalPortException extends Exception {
    /**
     * Constructs a IllegalPortException
     * @param port the unavailable port
     */
    public IllegalPortException(int port) {
        super("The " + port + " is not available.");
    }
}
