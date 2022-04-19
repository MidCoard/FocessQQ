package top.focess.qq.api.event.command;

import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandResult;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.util.IOHandler;

/**
 * Called after command executed
 */
public class CommandExecutedEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The args of this executor
     */
    @NotNull
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
     * The result
     */
    private final CommandResult result;
    /**
     * The command
     */
    private final Command command;

    /**
     * Constructs a CommandExecutedEvent
     *
     * @param command   the executed command
     * @param args      the args of the executor
     * @param ioHandler the input and output handler
     * @param sender    the executor
     * @param result    the result
     */
    public CommandExecutedEvent(Command command, @NotNull final String[] args, final IOHandler ioHandler, final CommandSender sender, final CommandResult result) {
        this.command = command;
        this.args = args;
        this.ioHandler = ioHandler;
        this.sender = sender;
        this.result = result;
    }

    public CommandResult getResult() {
        return this.result;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    @NotNull
    public String[] getArgs() {
        return this.args;
    }

    public IOHandler getIoHandler() {
        return this.ioHandler;
    }

    public Command getCommand() {
        return command;
    }
}
