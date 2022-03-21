package top.focess.qq.api.command;

import net.mamoe.mirai.contact.MemberPermission;

public enum CommandPermission {

    ADMINISTRATOR(MemberPermission.ADMINISTRATOR,3),
    OWNER(MemberPermission.OWNER,5),
    MEMBER(MemberPermission.MEMBER,0),
    FRIEND(OWNER);

    final MemberPermission permission;
    final int priority;

    CommandPermission(MemberPermission permission, int priority) {
        this.permission = permission;
        this.priority = priority;
    }

    CommandPermission(CommandPermission commandPermission) {
        this.permission = commandPermission.permission;
        this.priority = commandPermission.priority;
    }

    public boolean hasPermission(CommandPermission permission) {
        return this.priority >= permission.priority;
    }
}
