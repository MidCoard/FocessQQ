package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.plugin.Plugin;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.command.converter.PluginDataConverter;
import com.focess.api.util.IOHandler;
import com.focess.core.plugin.PluginClassLoader;

public class UnloadCommand extends Command {
    public UnloadCommand() {
        super("unload");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, data, ioHandler) -> {
            Plugin plugin = data.getPlugin();
            if (plugin == Main.getMainPlugin()) {
                ioHandler.output("Can't unload the Main Plugin, if you want to stop the server use stop command.");
                return CommandResult.REFUSE;
            }
            if (!(plugin.getClass().getClassLoader() instanceof PluginClassLoader)) {
                ioHandler.output("Plugin " + plugin.getName() + " is not loaded from PluginClassLoader");
                return CommandResult.REFUSE;
            }
            ioHandler.output("Unload " + plugin.getName());
            PluginClassLoader.disablePlugin(plugin);
            return CommandResult.ALLOW;
        }).setDataConverters(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: unload <plugin>");
    }


}
