package top.focess.qq.api.event.request;

import net.mamoe.mirai.contact.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.event.bot.BotEvent;

/**
 * Called when a friend-request comes
 */
public class FriendRequestEvent extends BotEvent {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The id of the stranger
     */
    private final long id;
    /**
     * The nickname of the stranger
     */
    private final String nick;
    /**
     * The group of the stranger where the application comes from
     */
    private final Group group;
    /**
     * The message of the application
     */
    private final String message;
    /**
     * The friend-request status
     */
    private Boolean accept;

    private boolean blacklist;

    /**
     * Constructs a FriendRequestEvent
     *
     * @param bot the bot
     * @param id the id of the stranger
     * @param nick the nickname of the stranger
     * @param group the group of the stranger where the application comes from
     * @param message the message of the application
     */
    public FriendRequestEvent(Bot bot, long id, String nick, @Nullable Group group, String message) {
        super(bot);
        this.id = id;
        this.nick = nick;
        this.group = group;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getNick() {
        return nick;
    }

    @Nullable
    public Group getGroup() {
        return group;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    /**
     * Accept this request
     */
    public void accept() {
        this.accept = true;
    }

    @Nullable
    public Boolean getAccept() {
        return accept;
    }

    /**
     * Refuse this request
     */
    public void refuse() {
        this.refuse(false);
    }

    /**
     * Refuse this request and add blacklist to it or not
     *
     * @param blacklist whether to add blacklist to
     */
    public void refuse(boolean blacklist) {
        this.accept = false;
        this.blacklist = blacklist;
    }

    /**
     * Indicate add this stranger to blacklist
     *
     * @return true if add this stranger to blacklist, false otherwise
     */
    public boolean isBlackList() {
        return blacklist;
    }
}
