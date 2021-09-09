package com.focess.api.event.chat;

import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;

public class GroupChatEvent extends ChatEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    private final Member member;
    private final OnlineMessageSource.Incoming.FromGroup source;

    public GroupChatEvent(Member member, MessageChain message, OnlineMessageSource.Incoming.FromGroup source) {
        super(message);
        this.member = member;
        this.source = source;
    }

    public Member getMember() {
        return member;
    }

    public Group getGroup() {
        return this.member.getGroup();
    }

    public OnlineMessageSource.Incoming.FromGroup getSource() {
        return source;
    }
}
