package com.focess.api.command;

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

    public static CommandPermission toCommandPermission(MemberPermission permission) {
        if (permission == MemberPermission.OWNER)
            return OWNER;
        else if (permission == MemberPermission.ADMINISTRATOR)
            return ADMINISTRATOR;
        else return MEMBER;
    }

    public boolean hasPermission(CommandPermission permission) {
        return this.priority >= permission.priority;
    }
}
