package top.focess.qq.api.command.converter;

import top.focess.qq.api.command.Command;

/**
 * Convert the String argument to Command argument
 */
public class CommandDataConverter extends NullDataConverter<Command> {

    /**
     * Convert the String argument to Command argument
     */
    public static final CommandDataConverter COMMAND_DATA_CONVERTER = new CommandDataConverter();

    @Override
    public Command convert(String s) {
        for (Command command:Command.getCommands())
            if (command.getName().equals(s))
                return command;
        return null;
    }

    @Override
    protected Class<Command> getTargetClass() {
        return Command.class;
    }
}