package top.focess.qq.api.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import top.focess.command.CommandPermission;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.CommandExecutor;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.contact.Member;
import top.focess.qq.api.bot.contact.Stranger;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.api.util.session.Session;
import top.focess.qq.core.listeners.ChatListener;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * This class present an executor to execute command. We can use it to distinguish different permissions.
 */
public class CommandSender extends top.focess.command.CommandSender {

    /**
     * Present CONSOLE or we call it Terminate
     */
    public static final CommandSender CONSOLE = new CommandSender();
    private static final Map<CommandSender, Session> SESSIONS = Maps.newHashMap();
    private final Member member;
    private final Friend friend;
    private final Stranger stranger;
    private final Bot bot;
    private final boolean isMember;
    private final boolean isFriend;
    private final boolean isStranger;

    private CommandSender() {
        super(top.focess.command.CommandPermission.OWNER);
        if (CONSOLE != null)
            throw new IllegalStateException("CommandSender.CONSOLE is not null");
        this.member = null;
        this.friend = null;
        this.stranger = null;
        this.bot = null;
        this.isFriend = false;
        this.isMember = false;
        this.isStranger = false;
    }

    /**
     * Present Friend
     *
     * @param friend the friend
     */
    @Deprecated
    public CommandSender(@NonNull final Friend friend) {
        super(CommandPermission.OWNER);
        this.member = null;
        this.stranger = null;
        this.friend = friend;
        this.bot = friend.getBot();
        this.isFriend = true;
        this.isMember = false;
        this.isStranger = false;
    }

    /**
     * Present Member in Group
     *
     * @param member the member
     */
    @Deprecated
    public CommandSender(@NonNull final Member member) {
        super(member.getPermission());
        this.member = member;
        this.stranger = null;
        this.friend = null;
        this.bot = member.getBot();
        this.isMember = true;
        this.isFriend = false;
        this.isStranger = false;
    }

    /**
     * Present Stranger
     *
     * @param stranger the stranger
     */
    @Deprecated
    public CommandSender(@NonNull final Stranger stranger) {
        super(CommandPermission.OWNER);
        this.member = null;
        this.friend = null;
        this.stranger = stranger;
        this.bot = stranger.getBot();
        this.isMember = false;
        this.isFriend = false;
        this.isStranger = true;
    }

    public static void clear(final Plugin plugin) {
        SESSIONS.values().stream().map(Session::getValues).forEach(map -> {
            final List<String> keys = Lists.newArrayList();
            map.forEach((key, value) -> {
                if (key.startsWith(plugin.getName() + ":"))
                    keys.add(key);
            });
            keys.forEach(map::remove);
        });
    }

    /**
     * Get the friend, or null if this CommandSender does not present a friend
     *
     * @return the friend
     */
    @Nullable
    public Friend getFriend() {
        return this.friend;
    }

    /**
     * Indicate this is a friend
     *
     * @return true if this CommandSender presents a friend, false otherwise
     */
    @EnsuresNonNullIf(expression = "getFriend()", result = true)
    public boolean isFriend() {
        return this.isFriend;
    }

    /**
     * Indicate this CommandSender owns the permission
     *
     * @param permission the compared permission
     * @return true if the permission of this CommandSender is higher or equivalent to the compared permission, false otherwise
     */
    public boolean hasPermission(final CommandPermission permission) {
        if (this.isAdministrator())
            return true;
        return super.hasPermission(permission);
    }

    /**
     * Get the member, or null if this CommandSender does not present a member
     *
     * @return the member
     */
    @Nullable
    public Member getMember() {
        return this.member;
    }

    /**
     * Indicate this is a member
     *
     * @return true if this CommandSender presents a member, false otherwise
     */
    @EnsuresNonNullIf(expression = "getMember()", result = true)
    public boolean isMember() {
        return this.isMember;
    }

    /**
     * Indicate this is an Administrator
     *
     * @return true if this CommandSender presents its id is equal to the id of the Administrator, false otherwise
     */
    public boolean isAdministrator() {
        if (FocessQQ.getAdministratorId() == null)
            return false;
        return this.isFriend ? this.friend.getId() == FocessQQ.getAdministratorId() : this.isMember && this.member.getId() == FocessQQ.getAdministratorId();
    }

    public String toString() {
        if (this.isFriend())
            return this.friend.getRawName() + "(" + this.friend.getId() + ") in " +this.bot.getId();
        else if (this.isMember())
            return this.member.getCardName() + "(" + this.member.getId() + ")" + "[" + this.member.getGroup().getId() + "] in " +this.bot.getId();
        else if (this.isStranger())
            return this.stranger.getRawName() + "(" + this.stranger.getId() + ") in " +this.bot.getId();
        else return "CONSOLE";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final CommandSender sender = (CommandSender) o;
        if (this.isMember() && sender.isMember()) {
            return sender.getMember() == this.getMember();
        } else if (this.isFriend() && sender.isFriend()) {
            return sender.getFriend() == this.getFriend();
        } else if (this.isStranger() && sender.isStranger())
            return sender.getStranger() == this.getStranger();
        else return this.isConsole() && sender.isConsole();
    }


    @Override
    public int hashCode() {
        int result = member != null ? member.hashCode() : 0;
        result = 31 * result + (friend != null ? friend.hashCode() : 0);
        result = 31 * result + (stranger != null ? stranger.hashCode() : 0);
        return result;
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
                if (CommandSender.this.isMember()) {
                    assert CommandSender.this.getMember() != null;
                    CommandSender.this.getMember().getGroup().sendMessage(output);
                }
                else if (CommandSender.this.isFriend()) {
                    assert CommandSender.this.getFriend() != null;
                    CommandSender.this.getFriend().sendMessage(output);
                }
                else if (CommandSender.this.isStranger()) {
                    assert CommandSender.this.getStranger() != null;
                    CommandSender.this.getStranger().sendMessage(output);
                } else throw new IllegalStateException("This CommandSender is not a member, friend or stranger");
            }

            @Override
            public synchronized boolean hasInput(boolean flag, int seconds) {
                ChatListener.registerInputListener(this, CommandSender.this, flag,IOHandler.SCHEDULER.run(() -> input((String) null), Duration.ofSeconds(seconds)));
                return super.hasInput(flag);
            }

            @Override
            public synchronized boolean hasInput(final boolean flag) {
                ChatListener.registerInputListener(this, CommandSender.this, flag, IOHandler.SCHEDULER.run(() -> input((String) null), Duration.ofMinutes(10)));
                return super.hasInput(flag);
            }

        };
    }

    /**
     * Execute command with this CommandSender
     *
     * @param command the command CommandSender execute
     * @see CommandLine#exec(CommandSender, String)
     * @return the result of the command
     */
    public Future<CommandResult> exec(final String command) {
        return CommandLine.exec(this, command);
    }

    /**
     * Get Session of the CommandSender. It can be used to store some data for future using.
     * But the data it stored will be lost after Bot exiting.
     *
     * @return Session of sender
     */
    public Session getSession() {
        if (SESSIONS.containsKey(this))
            return SESSIONS.get(this);
        else {
            final Session session = new Session(null);
            SESSIONS.put(this, session);
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
     * Indicate whether this is a stranger
     *
     * @return true if this CommandSender presents a stranger, false otherwise
     */
    @EnsuresNonNullIf(expression = "getStranger()", result = true)
    public boolean isStranger() {
        return this.isStranger;
    }

    /**
     * Get the stranger, or null if this CommandSender does not present a stranger
     *
     * @return the stranger
     */
    @Nullable
    public Stranger getStranger() {
        return this.stranger;
    }

    /**
     * Get the CommandSender by CommandExecutor
     *
     * @param executor the executor
     * @return the CommandSender
     */
    public static CommandSender of(final CommandExecutor executor) {
        if (executor instanceof Member)
            return new CommandSender((Member) executor);
        else if (executor instanceof Friend)
            return new CommandSender((Friend) executor);
        else if (executor instanceof Stranger)
            return new CommandSender((Stranger) executor);
        else return CONSOLE;
    }
}
