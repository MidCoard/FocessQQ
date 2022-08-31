package top.focess.qq.api.event.command;

import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.Cancellable;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

/**
 * Called before command executing
 */
@PermissionEnv(values = Permission.CANCEL_COMMAND_EXECUTE)
public class CommandPrepostEvent extends Event implements Cancellable {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The command
     */
    private final Command command;
    /**
     * The args of this executor
     */
    private final String[] args;

    /**
     * The input and output handler
     */
    private final IOHandler ioHandler;

    /**
     * The executor
     */
    private final CommandSender sender;

    /**
     * Indicate this event is cancelled
     */
    private boolean cancelled;

    /**
     * Constructs a CommandPrepostEvent
     *
     * @param sender    the executor
     * @param command   the Command
     * @param args      the data of this executor
     * @param ioHandler the input and output handler
     */
    public CommandPrepostEvent(final CommandSender sender, final Command command, final String[] args, final IOHandler ioHandler) {
        this.command = command;
        this.args = args;
        this.ioHandler = ioHandler;
        this.cancelled = false;
        this.sender = sender;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        Permission.checkPermission(Permission.CANCEL_COMMAND_EXECUTE);
        this.cancelled = cancelled;
    }

    public String[] getArgs() {
        return this.args;
    }

    public IOHandler getIoHandler() {
        return this.ioHandler;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public Command getCommand() {
        return this.command;
    }
}
