package top.focess.qq.api.command.converter;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.command.Command;

/**
 * Convert the String argument to Command argument
 */
public class CommandDataConverter extends NullDataConverter<Command> {

    /**
     * Convert the String argument to Command argument
     */
    public static final CommandDataConverter COMMAND_DATA_CONVERTER = new CommandDataConverter();

    @Nullable
    @Override
    public Command convert(final String s) {
        for (final Command command:Command.getCommands())
            if (command.getName().equals(s))
                return command;
        return null;
    }

    @Override
    protected Class<Command> getTargetClass() {
        return Command.class;
    }
}