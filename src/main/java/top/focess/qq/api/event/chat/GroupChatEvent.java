package top.focess.qq.api.event.chat;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Member;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.bot.message.MessageSource;
import top.focess.qq.api.event.ListenerHandler;

/**
 * Called when a member who chats in the group the bot is in
 */
public class GroupChatEvent extends ChatEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The member who chats in the group the bot is in
     */
    private final Member member;
    /**
     * Constructs a GroupChatEvent
     *
     * @param bot the bot
     * @param member the one who chats in the group the bot is in
     * @param message the chat message
     * @param source the source of the message
     */
    public GroupChatEvent(Bot bot, Member member, MessageChain message, MessageSource source) {
        super(bot,message, source);
        this.member = member;
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
}
