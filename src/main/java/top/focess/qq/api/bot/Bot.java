package top.focess.qq.api.bot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;

/**
 * This class is used to define a Mirai Bot Instance
 */
public interface Bot {

    /**
     * Relogin the bot
     *
     * @return true if the bot is online, false otherwise
     * @throws BotLoginException throw if the bot login failed
     */
    boolean relogin() throws BotLoginException;

    /**
     * Login the bot
     *
     * @return true if the bot is not online, false otherwise
     * @throws BotLoginException throw if the bot login failed
     */
    boolean login() throws BotLoginException;

    /**
     * Logout the bot
     *
     * @return true if the bot is online, false otherwise
     */
    boolean logout();

    /**
     * Get the friend with special id
     *
     * @param id the friend id
     * @return the friend with special id
     * @throws NullPointerException throw if the friend with special id does not exist
     */
    @NonNull
    Friend getFriendOrFail(long id);

    /**
     * Get the group with special id
     *
     * @param id the group id
     * @return the group with special id
     * @throws NullPointerException throw if the group with special id does not exist
     */
    @NonNull
    Group getGroupOrFail(long id);

    /**
     * Get the group with special id
     *
     * @param id the group id
     * @return the group with special id
     */
    @Nullable
    Group getGroup(long id);

    /**
     * Get the friend with special id
     *
     * @param id the friend id
     * @return the friend with special id
     */
    @Nullable
    Friend getFriend(long id);

    /**
     * Get all the friends
     *
     * @return all the friends
     */
    @NonNull
    @UnmodifiableView
    List<Friend> getFriends();

    /**
     * Get all the groups
     *
     * @return all the groups
     */
    @NonNull
    @UnmodifiableView
    List<Group> getGroups();

    /**
     * Indicate the bot is online
     *
     * @return true if the bot is online, false otherwise
     */
    boolean isOnline();

    /**
     * Get the bot itself as a friend
     *
     * @return the bot itself as a friend
     */
    @NonNull
    Friend getAsFriend();

    /**
     * Get the id of the bot
     *
     * @return the id of the bot
     */
    long getId();

    /**
     * Indicate this is the default bot
     *
     * @return true if this is the default bot, false otherwise
     */
    boolean isDefaultBot();

    /**
     * Get the plugin of the bot
     *
     * @return the plugin of the bot
     */
    Plugin getPlugin();

    /**
     * Indicate this is the Administrator
     *
     * @return true if this is the Administrator, false otherwise
     */
    boolean isAdministrator();

    /**
     * Indicate the bot is offline
     * @return true if the bot is offline, false otherwise
     */
    default boolean isOffline() {
        return !isOnline();
    }

    /**
     * Get the Bot Manager of the bot
     * @return the Bot Manager of the bot
     */
    BotManager getBotManager();
}
