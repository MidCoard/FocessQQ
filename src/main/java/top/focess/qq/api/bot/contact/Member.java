package top.focess.qq.api.bot.contact;

import top.focess.qq.api.command.CommandPermission;

/**
 * Represents a member in a group.
 */
public interface Member extends Contact,CommandExecutor {

    /**
     * Get the member's raw name (its nickname)
     *
     * @return the raw name
     */
    String getRawName();

    /**
     * Get the name in the group
     *
     * @return the name in the group
     */
    String getCardName();

    /**
     * Get the group
     *
     * @return the group
     */
    Group getGroup();

    /**
     * Get the member's permission in group
     *
     * @return the permission
     */
    CommandPermission getPermission();
}
