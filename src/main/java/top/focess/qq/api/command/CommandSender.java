package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Member;
import top.focess.qq.api.bot.contact.Stranger;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.session.Session;
import top.focess.qq.core.listeners.ChatListener;

import java.util.List;
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
    public CommandSender(@NonNull final Friend friend) {
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
    public CommandSender(@NonNull final Member member) {
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
    public CommandSender(@NonNull final Stranger stranger) {
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
        return this.friend;
    }

    /**
     * Indicate whether this is a Mirai Friend instance
     *
     * @return true if this CommandSender presents a Mirai Friend instance, false otherwise
     */
    @EnsuresNonNullIf(expression = "getFriend()", result = true)
    public boolean isFriend() {
        return this.isFriend;
    }


    /**
     * Indicate whether this CommandSender owns the permission
     *
     * @param permission the compared permission
     * @return true if the permission of this CommandSender is higher or equivalent to the compared permission, false otherwise
     */
    public boolean hasPermission(final CommandPermission permission) {
        if (this.isAdministrator())
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
        return this.member;
    }

    /**
     * Indicate whether this is a Mirai Member instance
     *
     * @return true if this CommandSender presents a Mirai Member instance, false otherwise
     */
    @EnsuresNonNullIf(expression = "getMember()", result = true)
    public boolean isMember() {
        return this.isMember;
    }

    /**
     * Indicate whether this is an Administrator
     *
     * @return true if this CommandSender presents its id is equal to the id of the Administrator, false otherwise
     */
    public boolean isAdministrator() {
        if (FocessQQ.getAdministratorId() == null)
            return false;
        return this.isFriend ? this.friend.getId() == FocessQQ.getAdministratorId() : this.isMember && this.member.getId() == FocessQQ.getAdministratorId();
    }

    /**
     * Get permission
     * @return permission of this sender
     */
    @NonNull
    public CommandPermission getPermission() {
        return this.permission;
    }

    public String toString() {
        if (this.isFriend())
            return this.friend.getRawName() + "(" + this.friend.getId() + ")";
        else if (this.isMember)
            return this.member.getCardName() + "(" + this.member.getId() + ")" + "[" + this.member.getGroup().getId() + "]";
        else return "CONSOLE";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final CommandSender sender = (CommandSender) o;
        if (this.isMember() && sender.isMember()) {
            return sender.getMember().getGroup().getId() == this.getMember().getGroup().getId() && sender.getMember().getId() == this.getMember().getId();
        } else if (this.isFriend() && sender.isFriend()) {
            return sender.getFriend().getId() == this.getFriend().getId();
        } else return this.isConsole() && sender.isConsole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.member == null ? null : this.member.getId(), this.friend == null ? null : this.friend.getId(), this.isMember, this.isFriend);
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
            public void output(final String output) {
                if (CommandSender.this.isMember())
                    CommandSender.this.getMember().getGroup().sendMessage(output);
                else if (CommandSender.this.isFriend())
                    CommandSender.this.getFriend().sendMessage(output);
            }

            @Override
            public boolean hasInput(final boolean flag) {
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
    public void exec(final String command) {
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
            final Session session = new Session(null);
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
        return this.bot;
    }

    /**
     * Indicate whether this is a Mirai Stranger instance
     *
     * @return true if this CommandSender presents a Mirai Stranger instance, false otherwise
     */
    public boolean isStranger() {
        return this.isStranger;
    }

    public static void clear(final Plugin plugin) {
        SESSIONS.values().stream().map(Session::getValues).forEach(map ->{
            final List<String> keys = Lists.newArrayList();
            map.forEach((key,value) -> {
               if (key.startsWith(plugin.getName() + ":"))
                   keys.add(key);
            });
            keys.forEach(map::remove);
        });
    }
}
