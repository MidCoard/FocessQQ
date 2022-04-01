package top.focess.qq.api.command;

import net.mamoe.mirai.contact.MemberPermission;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an executing permission of a command.
 */
public enum CommandPermission {

    /**
     * It is an Administrator permission in group
     */
    ADMINISTRATOR(MemberPermission.ADMINISTRATOR, 3),
    /**
     * It is an Owner permission in group
     */
    OWNER(MemberPermission.OWNER, 5),
    /**
     * It is a Member permission in group
     */
    MEMBER(MemberPermission.MEMBER, 0),
    /**
     * It is a Friend permission
     */
    FRIEND(OWNER);

    /**
     * The native permission
     */
    final MemberPermission permission;
    /**
     * The permission level
     */
    final int priority;

    CommandPermission(final MemberPermission permission, final int priority) {
        this.permission = permission;
        this.priority = priority;
    }

    CommandPermission(@NotNull final CommandPermission commandPermission) {
        this.permission = commandPermission.permission;
        this.priority = commandPermission.priority;
    }

    /**
     * Indicate this permission is higher than the comparing permission
     *
     * @param permission the comparing permission
     * @return true if this permission is higher than the comparing permission, false otherwise
     */
    @Contract(pure = true)
    public boolean hasPermission(@NotNull final CommandPermission permission) {
        return this.priority >= permission.priority;
    }
}
