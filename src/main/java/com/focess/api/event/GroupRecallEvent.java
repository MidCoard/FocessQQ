package com.focess.api.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public class GroupRecallEvent extends Event{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    private final Member member;
    private final int[] messageIds;

    public GroupRecallEvent(Member member, int[] messageIds) {
        this.member = member;
        this.messageIds = messageIds;
    }

    public Member getMember() {
        return member;
    }

    public int[] getMessageIds() {
        return messageIds;
    }

    public Group getGroup() {
        return this.member.getGroup();
    }
}
