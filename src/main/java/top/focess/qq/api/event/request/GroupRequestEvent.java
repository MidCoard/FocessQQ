package top.focess.qq.api.event.request;

import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

/**
 * Called when a group-request comes
 */
@PermissionEnv(values = {Permission.GROUP_REQUEST_ACCEPT, Permission.GROUP_REQUEST_IGNORE})
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
    @Nullable
    private final Friend invitor;
    /**
     * The request status
     */
    private Boolean accept;

    /**
     * Constructs a GroupRecallEvent
     *
     * @param bot     the bot
     * @param id      the id of the group
     * @param name    the name of the group
     * @param invitor the invitor of the request
     */
    public GroupRequestEvent(final Bot bot, final long id, final String name, @Nullable final Friend invitor) {
        super(bot);
        this.id = id;
        this.name = name;
        this.invitor = invitor;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public Friend getInvitor() {
        return this.invitor;
    }

    /**
     * Accept the request
     */
    public void accept() {
        Permission.checkPermission(Permission.GROUP_REQUEST_ACCEPT);
        this.accept = true;
    }

    /**
     * Ignore the request
     */
    public void ignore() {
        Permission.checkPermission(Permission.GROUP_REQUEST_IGNORE);
        this.accept = false;
    }

    public Boolean getAccept() {
        return this.accept;
    }
}
