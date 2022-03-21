package top.focess.qq.api.bot;

public interface Group extends Speaker{

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


}
