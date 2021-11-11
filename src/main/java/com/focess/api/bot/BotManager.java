package com.focess.api.bot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

/**
 * This class is used to manage all the bots.
 */
public interface BotManager {

    /**
     * Login and get the bot with id and password
     *
     * @param id the id of the bot
     * @param password the password of the bot
     * @return the bot
     */
    @NotNull Future<Bot> login(long id,String password);

    /**
     * Login and get the bot with id and password
     * This is a blocking method
     *
     * @param id the id of the bot
     * @param password the password of the bot
     * @return the bot
     */
    @NotNull
    Bot loginDirectly(long id, String password);

    /**
     * Login the bot
     *
     * @param bot the bot need to login
     * @return true if the bot is not online, false otherwise
     */
    boolean login(Bot bot);

    /**
     * Logout the bot
     * @param bot the bot need to logout
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
     */
    boolean relogin(@NotNull Bot bot);
}
