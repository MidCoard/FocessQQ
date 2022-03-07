package top.focess.qq.api.command.data;

import top.focess.qq.api.command.Command;

public class CommandBuffer extends DataBuffer<Command> {

    private final StringBuffer stringBuffer;

    public static CommandBuffer allocate(int size) {
        return new CommandBuffer(size);
    }

    public CommandBuffer(int size) {
        stringBuffer = StringBuffer.allocate(size);
    }

    @Override
    public void flip() {
        this.stringBuffer.flip();
    }

    @Override
    public void put(Command command) {
        this.stringBuffer.put(command.getName());
    }

    @Override
    public Command get() {
        String name = stringBuffer.get();
        for (Command command:Command.getCommands())
            if (command.getName().equals(name))
                return command;
        return null;
    }

    @Override
    public Command get(int index) {
        String name = stringBuffer.get(index);
        for (Command command:Command.getCommands())
            if (command.getName().equals(name))
                return command;
        return null;
    }
}