package top.focess.qq.api.bot.contact;

import top.focess.qq.api.bot.Bot;

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
