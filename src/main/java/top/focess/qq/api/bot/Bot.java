package top.focess.qq.api.bot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.api.bot.contact.*;
import top.focess.qq.api.bot.message.Audio;
import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.bot.message.TextMessage;
import top.focess.qq.api.plugin.Plugin;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

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
    default Friend getFriendOrFail(long id) {
        return Objects.requireNonNull(getFriend(id));
    }

    /**
     * Get the group with special id
     *
     * @param id the group id
     * @return the group with special id
     * @throws NullPointerException throw if the group with special id does not exist
     */
    @NonNull
    default Group getGroupOrFail(long id) {
        return Objects.requireNonNull(getGroup(id));
    }

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
        return !this.isOnline();
    }

    /**
     * Get the Bot Manager of the bot
     * @return the Bot Manager of the bot
     */
    BotManager getBotManager();

    /**
     * Send a message to the transmitter
     * @param transmitter the transmitter
     * @param message the message
     */
    void sendMessage(Transmitter transmitter, Message message);

    /**
     * Send a string message to the transmitter
     * @param transmitter the transmitter
     * @param message the message
     */
    default void sendMessage(Transmitter transmitter, String message) {
        this.sendMessage(transmitter, new TextMessage(message));
    }

    /**
     * Upload an image to the transmitter
     * @param transmitter the transmitter
     * @param resource the image resource
     * @return the image message or null if the image is not available
     */
    Image uploadImage(Transmitter transmitter, InputStream resource);

    /**
     * Upload an audio to the speaker
     * @param speaker the speaker
     * @param inputStream the audio resource
     * @return the audio message or null if the audio is not available
     */
    @Nullable
    Audio uploadAudio(Speaker speaker, InputStream inputStream);

    /**
     * Delete a friend
     * @param friend the friend
     */
    void deleteFriend(Friend friend);

    /**
     * Quit a group
     * @param group the group
     */
    void quitGroup(Group group);

    /**
     * Get the member by its group and id
     * @param group the group
     * @param id the member id
     * @return the member or null if not found
     */
    @Nullable
    Member getMember(Group group, long id);

    /**
     * Get the member by its group and id
     * @param group the group
     * @param id the member id
     * @return the member
     * @throws NullPointerException if the member is not found
     */
    default Member getMemberOrFail(Group group, long id) {
        return Objects.requireNonNull(this.getMember(group, id));
    }

    /**
     * Get the bot as a member in the group
     * @param group the group
     * @return the bot as a member in the group
     */
    Member getAsMember(Group group);

    /**
     * Get all the members in the group
     * @param group the group
     * @return all the members as a list
     */
    List<Member> getMembers(Group group);

    /**
     * Get the stranger by its specific id
     * @param id the id
     * @return the stranger or null if not found
     */
    @Nullable Stranger getStranger(long id);

    /**
     * Get the stranger by its specific id
     * @param id the id
     * @return the stranger
     * @throws NullPointerException if the stranger is not found
     */
    default Stranger getStrangerOrFail(long id) {
        return Objects.requireNonNull(this.getStranger(id));
    }

    /**
     * Get the other client by its specific id
     * @param id the id
     * @return the other client
     * @throws NullPointerException if the other client is not found
     */
    default OtherClient getOtherClientOrFail(long id) {
        return Objects.requireNonNull(this.getOtherClient(id));
    }

    /**
     * Get the other client by its specific id
     * @param id the id
     * @return the other client or null if not found
     */
    @Nullable
    OtherClient getOtherClient(long id);

    BotProtocol getBotProtocol();
}
