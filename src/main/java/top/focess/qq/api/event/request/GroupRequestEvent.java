package top.focess.qq.api.event.request;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.bot.BotEvent;
import top.focess.qq.api.event.ListenerHandler;
import net.mamoe.mirai.contact.Friend;

/**
 * Called when a group-request comes
 */
public class GroupRequestEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();
    /**
     * The id of the group
     */
    private final long id;
    /**
     * The name of the group
     */
    private final String name;
    /**
     * The invitor of the request
     */
    private final Friend invitor;
    /**
     * The request status
     */
    private Boolean accept;

    /**
     * Constructs a GroupRecallEvent
     *
     * @param bot the bot
     * @param id the id of the group
     * @param name the name of the group
     * @param invitor the invitor of the request
     */
    public GroupRequestEvent(Bot bot, long id, String name, Friend invitor) {
        super(bot);
        this.id = id;
        this.name = name;
        this.invitor = invitor;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Friend getInvitor() {
        return invitor;
    }

    /**
     * Accept the request
     */
    public void accept() {
        this.accept = true;
    }

    /**
     * Ignore the request
     */
    public void ignore() {
        this.accept = false;
    }

    public Boolean getAccept() {
        return accept;
    }
}