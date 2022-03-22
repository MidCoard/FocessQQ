package top.focess.qq.api.event.message;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Group;
import top.focess.qq.api.bot.Member;
import top.focess.qq.api.bot.message.MessageChain;
import top.focess.qq.api.bot.message.MessageSource;
import top.focess.qq.api.event.ListenerHandler;

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
     * Constructs a GroupMessageEvent
     *
     * @param bot the bot
     * @param member the one who chats in the group the bot is in
     * @param message the chat message
     * @param source the source of the message
     */
    public GroupMessageEvent(Bot bot, Member member, MessageChain message, MessageSource source) {
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
