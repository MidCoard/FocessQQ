package com.focess.api.command;

import com.focess.api.util.IOHandler;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;

import java.util.Objects;

public class CommandSender {

    public static final CommandSender CONSOLE = new CommandSender(new MemberOrConsoleOrFriend()) {
        @Override
        public IOHandler getIOHandler() {
            return IOHandler.IO_HANDLER;
        }
    };

    private final Member member;
    private final Friend friend;
    private final boolean isMember;
    private final boolean isFriend;
    private final MemberPermission permission;

    public Friend getFriend() {
        return friend;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public CommandSender(MemberOrConsoleOrFriend memberOrConsoleOrFriend) {
        this.member = memberOrConsoleOrFriend.member;
        this.friend = memberOrConsoleOrFriend.friend;
        this.isMember = memberOrConsoleOrFriend.isMember;
        this.isFriend = memberOrConsoleOrFriend.isFriend;
        this.permission = memberOrConsoleOrFriend.isMember ? this.member.getPermission() : MemberPermission.OWNER;
    }

    public static CommandSender getCommandSender(MemberOrConsoleOrFriend memberOrConsoleOrFriend) {
        return new CommandSender(memberOrConsoleOrFriend);
    }


    public boolean hasPermission(MemberPermission permission) {
        if (isAuthor())
            return true;
        switch (permission) {
            case MEMBER:
                return true;
            case ADMINISTRATOR:
                return this.getPermission() == MemberPermission.ADMINISTRATOR || this.getPermission() == MemberPermission.OWNER;
            case OWNER:
                return this.getPermission() == MemberPermission.OWNER;
        }
        return false;
    }

    public Member getMember() {
        return member;
    }

    public boolean isMember() {
        return isMember;
    }

    public boolean isAuthor() {
        return this.isFriend ? this.friend.getId() == 2624646185L : isMember && this.member.getId() == 2624646185L;
    }

    public MemberPermission getPermission() {
        return permission;
    }

    public String toString() {
        if (this.isFriend())
            return friend.getNick() + "(" + this.friend.getId() + ")";
        else if (this.isMember)
            return member.getNameCard() + "(" + this.member.getId() + ")" + "[" + this.member.getGroup().getId() + "]";
        else return "CONSOLE";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandSender sender = (CommandSender) o;
        if (this.isMember() && sender.isMember()) {
            return sender.getMember().getGroup() == this.getMember().getGroup() && sender.getMember().getId() == this.getMember().getId();
        }
        else if (this.isFriend() && sender.isFriend()) {
            return sender.getFriend().getId() == this.getFriend().getId();
        }
        else return this.isConsole() && sender.isConsole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(member == null ? null:member.getId(), friend == null ? null : friend.getId(), isMember, isFriend);
    }

    public boolean isSimilar(CommandSender sender) {
       return this.equals(sender);
    }

    public boolean isConsole() {
        return !isFriend() && !isMember();
    }

    public static class MemberOrConsoleOrFriend {
        private final boolean isMember;
        private final Member member;
        private final boolean isFriend;
        private final Friend friend;

        public MemberOrConsoleOrFriend(Friend friend) {
            this.isFriend = true;
            this.isMember = false;
            this.member = null;
            this.friend = friend;
        }

        public MemberOrConsoleOrFriend(Member member) {
            this.isMember = true;
            this.isFriend = false;
            this.member = member;
            this.friend = null;
        }

        public MemberOrConsoleOrFriend() {
            this.isMember = false;
            this.isFriend = false;
            this.member = null;
            this.friend = null;
        }
    }

    public IOHandler getIOHandler() {
        return IOHandler.getIoHandlerByCommandSender(this);
    }

}
