package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate a bot login failed
 */
public class BotLoginException extends RuntimeException {

    /**
     * Constructs a BotLoginException
     * @param id the id of the login failed bot
     */
    public BotLoginException(long id) {
        super("Bot " + id + " login failed");
    }
}
