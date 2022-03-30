package top.focess.qq.api.command.data;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.command.Command;

public class CommandBuffer extends DataBuffer<Command> {

    private final StringBuffer stringBuffer;

    public static CommandBuffer allocate(final int size) {
        return new CommandBuffer(size);
    }

    public CommandBuffer(final int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

    @Override
    public void flip() {
        this.stringBuffer.flip();
    }

    @Override
    public void put(final Command command) {
        this.stringBuffer.put(command.getName());
    }

    @Nullable
    @Override
    public Command get() {
        final String name = this.stringBuffer.get();
        for (final Command command:Command.getCommands())
            if (command.getName().equals(name))
                return command;
        return null;
    }

    @Nullable
    @Override
    public Command get(final int index) {
        final String name = this.stringBuffer.get(index);
        for (final Command command:Command.getCommands())
            if (command.getName().equals(name))
                return command;
        return null;
    }
}