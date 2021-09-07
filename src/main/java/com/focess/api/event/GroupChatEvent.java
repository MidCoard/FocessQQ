package com.focess.api.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class GroupChatEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    private final Member member;
    private final MessageChain message;

    public GroupChatEvent(Member member, MessageChain message) {

        this.member = member;
        this.message = message;
    }

    public Member getMember() {
        return member;
    }

    public MessageChain getMessage() {
        return message;
    }

    public Group getGroup() {
        return this.member.getGroup();
    }
}
