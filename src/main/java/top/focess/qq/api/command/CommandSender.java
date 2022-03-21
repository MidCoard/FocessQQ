package top.focess.qq.api.command;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Friend;
import top.focess.qq.api.bot.Member;
import top.focess.qq.api.bot.Stranger;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.session.Session;
import top.focess.qq.core.listeners.ChatListener;

import java.util.Map;
import java.util.Objects;

/**
 * This class present an executor to execute command. We can use it to distinguish different permissions.
 */
public class CommandSender {

    private static final Map<CommandSender, Session> SESSIONS = Maps.newHashMap();

    /**
     * Present CONSOLE or we call it Terminate
     */
    public static final CommandSender CONSOLE = new CommandSender();

    private final Member member;
    private final Friend friend;
    private final Stranger stranger;
    private final Bot bot;
    private final boolean isMember;
    private final boolean isFriend;
    private final CommandPermission permission;
    private final boolean isStranger;

    private CommandSender() {
        if (CONSOLE != null)
            throw new IllegalStateException("CommandSender.CONSOLE is not null");
        this.member = null;
        this.friend = null;
        this.stranger = null;
        this.bot = null;
        this.isFriend = false;
        this.isMember = false;
        this.isStranger = false;
        this.permission = CommandPermission.OWNER;
    }

    /**
     * Present Friend
     *
     * @param friend the Mirai Friend instance
     */
    public CommandSender(@NonNull Friend friend) {
        this.member = null;
        this.stranger = null;
        this.friend = friend;
        this.bot = FocessQQ.getBotManager().getBot(friend.getBot().getId());
        this.isFriend = true;
        this.isMember = false;
        this.isStranger = false;
        this.permission = CommandPermission.OWNER;
    }

    /**
     * Present Member in Group
     *
     * @param member The Mirai Member Instance
     */
    public CommandSender(@NonNull Member member) {
        this.member = member;
        this.stranger = null;
        this.friend = null;
        this.bot = FocessQQ.getBotManager().getBot(member.getBot().getId());
        this.isMember = true;
        this.isFriend = false;
        this.isStranger = false;
        this.permission = member.getPermission();
    }

    /**
     * Present Stranger
     *
     * @param stranger The Mirai Stranger Instance
     */
    public CommandSender(@NonNull Stranger stranger) {
        this.member = null;
        this.friend = null;
        this.stranger = stranger;
        this.bot = FocessQQ.getBotManager().getBot(this.stranger.getBot().getId());
        this.isMember = false;
        this.isFriend = false;
        this.isStranger = true;
        this.permission = CommandPermission.OWNER;
    }

    /**
     * Get the Mirai Friend instance, or null if this CommandSender does not present a Mirai Friend instance.
     *
     * @return the Mirai Friend instance
     */
    @Nullable
    public Friend getFriend() {
        return friend;
    }

    /**
     * Indicate whether this is a Mirai Friend instance
     *
     * @return true if this CommandSender presents a Mirai Friend instance, false otherwise
     */
    @EnsuresNonNullIf(expression = "getFriend()", result = true)
    public boolean isFriend() {
        return isFriend;
    }


    /**
     * Indicate whether this CommandSender owns the permission
     *
     * @param permission the compared permission
     * @return true if the permission of this CommandSender is higher or equivalent to the compared permission, false otherwise
     */
    public boolean hasPermission(CommandPermission permission) {
        if (isAdministrator())
            return true;
        return this.permission.hasPermission(permission);
    }


    /**
     * Get the Mirai Member instance, or null if this CommandSender does not present a Mirai Member instance.
     *
     * @return the Mirai Member instance
     */
    @Nullable
    public Member getMember() {
        return member;
    }

    /**
     * Indicate whether this is a Mirai Member instance
     *
     * @return true if this CommandSender presents a Mirai Member instance, false otherwise
     */
    @EnsuresNonNullIf(expression = "getMember()", result = true)
    public boolean isMember() {
        return isMember;
    }

    /**
     * Indicate whether this is an Administrator
     *
     * @return true if this CommandSender presents its id is equal to the id of the Administrator, false otherwise
     */
    public boolean isAdministrator() {
        if (FocessQQ.getAdministratorId() == null)
            return false;
        return this.isFriend ? this.friend.getId() == FocessQQ.getAdministratorId() : isMember && this.member.getId() == FocessQQ.getAdministratorId();
    }

    /**
     * Get permission
     * @return permission of this sender
     */
    @NonNull
    public CommandPermission getPermission() {
        return permission;
    }

    public String toString() {
        if (this.isFriend())
            return friend.getRawName() + "(" + this.friend.getId() + ")";
        else if (this.isMember)
            return member.getCardName() + "(" + this.member.getId() + ")" + "[" + this.member.getGroup().getId() + "]";
        else return "CONSOLE";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandSender sender = (CommandSender) o;
        if (this.isMember() && sender.isMember()) {
            return sender.getMember().getGroup().getId() == this.getMember().getGroup().getId() && sender.getMember().getId() == this.getMember().getId();
        } else if (this.isFriend() && sender.isFriend()) {
            return sender.getFriend().getId() == this.getFriend().getId();
        } else return this.isConsole() && sender.isConsole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(member == null ? null : member.getId(), friend == null ? null : friend.getId(), isMember, isFriend);
    }

    /**
     * Indicate whether it is CONSOLE
     *
     * @return true if it is {@link CommandSender#CONSOLE}, false otherwise
     */
    public boolean isConsole() {
        return this == CONSOLE;
    }

    /**
     * Get the receiver by this CommandSender
     *
     * @return the receiver by this CommandSender
     */
    public IOHandler getIOHandler() {
        if (this.isConsole())
            return IOHandler.getConsoleIoHandler();
        return new IOHandler() {

            @Override
            public void output(String output) {
                if (isMember())
                    getMember().getGroup().sendMessage(output);
                else if (isFriend())
                    getFriend().sendMessage(output);
            }

            @Override
            public boolean hasInput(boolean flag) {
                ChatListener.registerInputListener(this, CommandSender.this, flag);
                while (!this.flag) ;
                return true;
            }

        };
    }

    /**
     * Execute command with this CommandSender
     *
     * @see CommandLine#exec(CommandSender, String)
     * @param command the command CommandSender execute
     */
    public void exec(String command) {
        CommandLine.exec(this, command);
    }

    /**
     * Get Session of a special CommandSender. It can be used to store some data for future using.
     * But the data it stored will be lost after Bot exiting.
     *
     * @return Session of sender
     */
    public Session getSession() {

        if (SESSIONS.containsKey(this))
            return SESSIONS.get(this);
        else {
            Session session = new Session(null);
            SESSIONS.put(this,session);
            return session;
        }
    }

    /**
     * Get the bot
     *
     * @return the bot
     */
    @Nullable
    public Bot getBot() {
        return bot;
    }

    /**
     * Indicate whether this is a Mirai Stranger instance
     *
     * @return true if this CommandSender presents a Mirai Stranger instance, false otherwise
     */
    public boolean isStranger() {
        return isStranger;
    }
}
