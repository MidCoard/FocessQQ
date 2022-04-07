package top.focess.qq.api.command.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.focess.command.data.DataBuffer;
import top.focess.command.data.StringBuffer;
import top.focess.qq.api.command.Command;

/**
 * Represent a buffer of Command.
 */
public class CommandBuffer extends DataBuffer<Command> {

    private final StringBuffer stringBuffer;

    public CommandBuffer(final int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

    /**
     * Allocate a CommandBuffer with fixed size
     *
     * @param size the target buffer size
     * @return a CommandBuffer with fixed size
     */
    @NotNull
    @Contract("_ -> new")
    public static CommandBuffer allocate(final int size) {
        return new CommandBuffer(size);
    }

    @Override
    public void flip() {
        this.stringBuffer.flip();
    }

    @Override
    public void put(@NotNull final Command command) {
        this.stringBuffer.put(command.getName());
    }

    @NotNull
    @Override
    public Command get() {
        final String name = this.stringBuffer.get();
        for (final Command command : Command.getCommands())
            if (command.getName().equals(name))
                return command;
        throw new IllegalArgumentException("Command: " + name + " is not found");
    }

    @NotNull
    @Override
    public Command get(final int index) {
        final String name = this.stringBuffer.get(index);
        for (final Command command : Command.getCommands())
            if (command.getName().equals(name))
                return command;
        throw new IllegalArgumentException("Command: " + name + " is not found");
    }
}