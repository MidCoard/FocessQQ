package com.focess.api.event.message;

import com.focess.api.bot.Bot;
import com.focess.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;

/**
 * Called when a member who chats in the group the bot is in (this does not execute any commands)
 */
public class GroupMessageEvent extends MessageEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The member who chats in the group the bot is in
     */
    private final Member member;

    /**
     * The source of the message
     */
    private final OnlineMessageSource.Incoming.FromGroup source;

    /**
     * Constructs a GroupMessageEvent
     *
     * @param member the one who chats in the group the bot is in
     * @param message the chat message
     * @param source the source of the message
     */
    public GroupMessageEvent(Bot bot, Member member, MessageChain message, OnlineMessageSource.Incoming.FromGroup source) {
        super(bot,message);
        this.member = member;
        this.source = source;
    }

    public Member getMember() {
        return member;
    }

    /**
     * Get the Group Mirai instance of the member
     *
     * @return the Group Mirai instance of the member
     */
    public Group getGroup() {
        return this.member.getGroup();
    }

    public OnlineMessageSource.Incoming.FromGroup getSource() {
        return source;
    }
}
