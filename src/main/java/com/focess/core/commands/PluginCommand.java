package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.plugin.Plugin;
import com.focess.api.util.IOHandler;

public class PluginCommand extends Command {

    public PluginCommand() {
        super("plugin");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0,(sender,data,ioHandler)->{
            StringBuilder stringBuilder = new StringBuilder("The following plugins are: ");
            for (Plugin plugin: Main.getPlugins())
                stringBuilder.append(' ').append(plugin.getName());
            ioHandler.output(stringBuilder.toString());
            return CommandResult.ALLOW;
        },"list");
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        ioHandler.output("Use: plugin list");
    }
}
