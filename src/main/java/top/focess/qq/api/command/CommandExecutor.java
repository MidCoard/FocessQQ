package top.focess.qq.api.command;


import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandResult;
import top.focess.qq.api.util.IOHandler;
import top.focess.command.DataCollection;

/**
 * Represents a command executor to define how to execute command.
 * <p>
 * This is a functional interface whose functional method is {@link CommandExecutor#execute(CommandSender, DataCollection, IOHandler)}.
 */
@FunctionalInterface
public interface CommandExecutor extends top.focess.command.CommandExecutor {
    /**
     * Used to execute the command under certain conditions
     *
     * @param sender         the executor of the command
     * @param dataCollection parse the arguments the command received
     * @param ioHandler      the receiver of the command
     * @return the result of this execution
     */
    @NotNull
    CommandResult execute(CommandSender sender, DataCollection dataCollection, IOHandler ioHandler);

    @Override
    @NotNull
    default CommandResult execute(final top.focess.command.CommandSender commandSender, final DataCollection dataCollection, @NotNull final top.focess.command.IOHandler ioHandler) {
        return this.execute((CommandSender) commandSender, dataCollection,(IOHandler) ioHandler);
    }

}
