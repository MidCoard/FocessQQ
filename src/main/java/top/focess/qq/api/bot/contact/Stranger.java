package top.focess.qq.api.bot.contact;

/**
 * Represents a stranger.
 */
public interface Stranger extends Transmitter,CommandExecutor {

    /**
     * Get the stranger's raw name (its nickname)
     *
     * @return the raw name
     */
    String getRawName();
}
