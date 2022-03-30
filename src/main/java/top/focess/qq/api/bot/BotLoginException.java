package top.focess.qq.api.bot;

import java.io.IOException;

/**
 * Thrown to indicate a bot login failed
 */
public class BotLoginException extends IOException {


    /**
     * Constructs a BotLoginException
     * @param id the id of the login failed bot
     */
    public BotLoginException(final long id) {
        super("Bot " + id + " login failed.");
    }

    /**
     * Constructs a BotLoginException
     * @param id the id of the login failed bot
     * @param e the cause of the exception
     */
    public BotLoginException(final long id, final Exception e) {
        super("Bot " + id + " login failed.", e);
    }

    public BotLoginException(final long id, final String message) {
        super("Bot " + id + " login failed: " + message);
    }
}
