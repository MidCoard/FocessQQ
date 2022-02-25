package com.focess.core.commands;

import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.command.converter.CommandDataConverter;
import com.focess.api.util.IOHandler;


public class CommandCommand extends Command {

    public CommandCommand() {
        super("command");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0,(sender,data,ioHandler)->{
            StringBuilder stringBuilder = new StringBuilder("The following commands are: ");
            for (Command command:Command.getCommands())
                stringBuilder.append(' ').append(command.getName());
            ioHandler.output(stringBuilder.toString());
                return CommandResult.ALLOW;
        },"list");
        this.addExecutor(1,(sender,data,ioHandler) ->{
            Command command = data.get(Command.class);
            command.unregister();
            ioHandler.output("Command " + command.getName() + " has been unregistered.");
            return CommandResult.ALLOW;
        },"unload").setDataConverters(CommandDataConverter.COMMAND_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
            ioHandler.output("Use: command list\n" +
                    "Use: command unload [command]");
    }


}
