package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate a bot login failed
 */
public class BotLoginException extends RuntimeException {


    //todo rename some of the exceptions
    /**
     * Constructs a BotLoginException
     * @param id the id of the login failed bot
     */
    public BotLoginException(long id) {
        super("Bot " + id + " login failed.");
    }

    /**
     * Constructs a BotLoginException
     * @param id the id of the login failed bot
     * @param e the cause of the exception
     */
    public BotLoginException(long id, Exception e) {
        super("Bot " + id + " login failed.", e);
    }

    public BotLoginException(long id, String message) {
        super("Bot " + id + " login failed: " + message);
    }
}
