package top.focess.qq.api.event.recall;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;

/**
 * Called when a Member recall a message in a Group
 */
public class GroupRecallEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The member who send the message
     */
    private final Member member;
    /**
     * The message ids
     */
    private final int[] messageIds;
    /**
     * The member who recalls the message
     */
    @Nullable
    private final Member operator;

    /**
     * Constructs a GroupRecallEvent
     *
     * @param bot the bot
     * @param member the member who send the message
     * @param messageIds the message ids
     * @param operator the member who recalls the message
     */
    public GroupRecallEvent(Bot bot, Member member, int[] messageIds, @Nullable Member operator) {
        super(bot);
        this.member = member;
        this.messageIds = messageIds;
        this.operator = operator;
    }

    @NonNull
    public Member getMember() {
        return member;
    }

    public int[] getMessageIds() {
        return messageIds;
    }

    /**
     * Get the Group where the member recalls the message
     *
     * @return the Group where the member recalls the message
     */
    @NonNull
    public Group getGroup() {
        return this.member.getGroup();
    }

    @Nullable
    public Member getOperator() {
        return operator;
    }
}
