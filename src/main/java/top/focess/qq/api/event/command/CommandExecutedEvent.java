package top.focess.qq.api.event.command;

import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.event.Event;
import top.focess.qq.api.event.ListenerHandler;
import top.focess.qq.api.util.IOHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Called after command executed
 */
public class CommandExecutedEvent extends Event {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The Executor
     */
    private final Command.Executor executor;

    /**
     * The args of this executor
     */
    private final @NotNull String[] args;

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
     * Constructs a CommandExecutedEvent
     * @param executor the Executor
     * @param args the args of the executor
     * @param ioHandler the input and output handler
     * @param sender the executor
     * @param result the result
     */
    public CommandExecutedEvent(Command.Executor executor, @NotNull String[] args, IOHandler ioHandler, CommandSender sender, CommandResult result) {
        this.executor = executor;
        this.args = args;
        this.ioHandler = ioHandler;
        this.sender = sender;
        this.result = result;
    }

    public CommandResult getResult() {
        return result;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Command.Executor getExecutor() {
        return executor;
    }

    public @NotNull String[] getArgs() {
        return args;
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }
}
