package top.focess.qq.api.bot.contact;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * Represents a group.
 */
public interface Group extends Speaker {

    /**
     * Quit the group
     */
    void quit();

    /**
     * Get the group's member
     *
     * @param id the member's id
     * @return the member or null if not found
     */
    @Nullable
    Member getMember(long id);

    /**
     * Get the group's member
     *
     * @param id the member's id
     * @return the member
     * @throws NullPointerException if not found
     */
    Member getMemberOrFail(long id);

    /**
     * Get the group's all members
     *
     * @return the all members
     */
    @NonNull
    @UnmodifiableView
    List<Member> getMembers();


    /**
     * Get the bot self as a member
     *
     * @return the bot self as a member
     */
    Member getAsMember();

}
