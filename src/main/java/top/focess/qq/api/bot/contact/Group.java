package top.focess.qq.api.bot.contact;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public interface Group extends Speaker {

    /**
     * Quit the group
     */
    void quit();

    /**
     * Get the group's member
     * @param id the member's id
     * @return the member or null if not found
     */
    Member getMember(long id);

    /**
     * Get the group's member
     * @param id the member's id
     * @return the member
     * @throws NullPointerException if not found
     */
    Member getMemberOrFail(long id);

    /**
     * Get the group's all members
     * @return the all members
     */
    @NonNull
    List<Member> getMembers();

}
