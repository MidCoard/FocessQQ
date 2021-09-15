package com.focess.api.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public class GroupRecallEvent extends Event{

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    private final Member member;
    private final int[] messageIds;
    private final Member operator;

    public GroupRecallEvent(Member member, int[] messageIds, Member operator) {
        this.member = member;
        this.messageIds = messageIds;
        this.operator = operator;
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

    public Member getOperator() {
        return operator;
    }
}
