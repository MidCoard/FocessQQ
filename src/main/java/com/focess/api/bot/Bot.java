package com.focess.api.bot;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class is used to define a Mirai Bot Instance
 */
public interface Bot {

    /**
     * Get the Mirai Bot Instance
     *
     * @return the Mirai Bot Instance
     */
    @NotNull
    net.mamoe.mirai.Bot getNativeBot();

    /**
     * Relogin the bot
     *
     * @return true if the bot is online, false otherwise
     */
    boolean relogin();

    /**
     * Login the bot
     *
     * @return true if the bot is not online, false otherwise
     */
    boolean login();

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
    @NotNull
    Friend getFriendOrFail(long id);

    /**
     * Get the group with special id
     * @param id the group id
     * @return the group with special id
     */
    @Nullable
    Group getGroup(long id);

    /**
     * Get the friend with special id
     * @param id the friend id
     * @return the friend with special id
     */
    @Nullable
    Friend getFriend(long id);

    /**
     * Get all the friends
     * @return all the friends
     */
    @NotNull
    List<Friend> getFriends();

    /**
     * Get all the groups
     * @return all the groups
     */
    @NotNull
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
    @NotNull
    Friend getAsFriend();

    /**
     * Get the id of the bot
     *
     * @return the id of the bot
     */
    long getId();
}
