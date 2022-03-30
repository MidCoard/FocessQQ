package top.focess.qq.api.bot.contact;

import top.focess.qq.api.bot.Bot;

/**
 * Represents a contact, which means a {@link Stranger}, a {@link Friend}, a {@link Group}, a {@link OtherClient} or a {@link Member} in a group.
 */
public interface Contact {

    /**
     * Get the contact id
     *
     * @return the contact id
     */
    long getId();

    /**
     * Get the contact name
     *
     * @return the contact name
     */
    String getName();

    /**
     * Get the contact bot
     *
     * @return the contact bot
     */
    Bot getBot();
}
