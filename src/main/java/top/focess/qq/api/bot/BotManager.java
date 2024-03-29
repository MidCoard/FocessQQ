package top.focess.qq.api.bot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;
import java.util.concurrent.Future;

/**
 * This class is used to manage all the bots.
 */
public interface BotManager {


    /**
     * Login and get the bot with id and password
     * @param id the id of the bot
     * @param password the password of the bot
     * @param plugin the plugin
     * @param botProtocol the bot protocol
     * @return the bot
     */
    Future<Bot> login(long id, String password, Plugin plugin, BotProtocol botProtocol);

    /**
     * Login and get the bot with id and password
     *
     * @param id       the id of the bot
     * @param password the password of the bot
     * @param plugin   the plugin
     * @return the bot
     */
    @NotNull
    default Future<Bot> login(final long id, final String password, final Plugin plugin) {
        return this.login(id, password, plugin, BotProtocol.ANDROID_WATCH);
    }

    /**
     * Login and get the bot with id and password
     * <p>
     * Note: this is a blocking method.
     *
     * @param id       the id of the bot
     * @param password the password of the bot
     * @param plugin   the plugin
     * @param botProtocol the bot protocol
     * @return the bot
     * @throws BotLoginException if the bot login failed
     */
    @NotNull
    Bot loginDirectly(long id, String password, Plugin plugin, BotProtocol botProtocol) throws BotLoginException;


    /**
     * Login and get the bot with id and password
     * <p>
     * Note: this is a blocking method.
     *
     * @param id       the id of the bot
     * @param password the password of the bot
     * @param plugin   the plugin
     * @return the bot
     * @throws BotLoginException if the bot login failed
     */
    @NotNull
    default Bot loginDirectly(final long id, final String password, final Plugin plugin) throws BotLoginException {
        return this.loginDirectly(id, password, plugin, BotProtocol.ANDROID_WATCH);
    }

    /**
     * Login the bot
     *
     * @param bot the bot need to log in
     * @return true if the bot is not online, false otherwise
     * @throws BotLoginException if the bot login failed
     */
    boolean login(Bot bot) throws BotLoginException;

    /**
     * Logout the bot
     *
     * @param bot the bot need to log out
     * @return true if the bot is online, false otherwise
     */
    boolean logout(@NotNull Bot bot);

    /**
     * Get the bot with special id
     *
     * @param id the id of the bot
     * @return the bot with special id
     */
    @Nullable
    Bot getBot(long id);

    /**
     * Relogin the bot
     *
     * @param bot the bot need to relogin
     * @return true if the bot is online, false otherwise
     * @throws BotLoginException if the bot login failed
     */
    boolean relogin(@NotNull Bot bot) throws BotLoginException;

    /**
     * Get the list of bots
     *
     * @return the list of bots
     */
    @UnmodifiableView
    List<Bot> getBots();

    /**
     * Remove the bot
     *
     * @param id the bot id
     * @return the previous bot
     */
    Bot remove(long id);

    /**
     * Remove all the bots
     */
    void removeAll();

    /**
     * Remove all the bots logined by the plugin
     * @param plugin the plugin
     */
    void remove(Plugin plugin);
}
