package com.focess.commands;

import com.focess.Main;
import com.focess.api.Plugin;
import com.focess.api.command.*;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;

public class UnloadCommand extends Command {
    public UnloadCommand() {
        super("unload", Lists.newArrayList());
    }

    @Override
    public void init() {
        this.addExecutor(1, (sender, data, ioHandler) -> {
            if (sender.isConsole()) {
                Plugin plugin = data.getPlugin();
                ioHandler.output("Unload " + plugin.getName());
                if (!(plugin.getClass().getClassLoader() instanceof LoadCommand.PluginClassLoader))
                    Main.getLogger().debug("Plugin " + plugin.getName() + " is not loaded from PluginClassLoader");
                LoadCommand.disablePlugin(plugin);
                System.gc();
                return CommandResult.ALLOW;
            }
            return CommandResult.REFUSE;
        }).setDataConverters(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        if (commandSender.isConsole())
            ioHandler.output("Use: unload [plugin-name]");
    }

    public static class PluginDataConverter extends DataConverter<Plugin> {

        public static final PluginDataConverter PLUGIN_DATA_CONVERTER = new PluginDataConverter();

        @Override
        protected boolean accept(String arg) {
            return Plugin.getPlugin(arg) != null;
        }

        @Override
        public Plugin convert(String arg) {
            return Plugin.getPlugin(arg);
        }

        @Override
        protected void connect(DataCollection dataCollection, Plugin arg) {
            dataCollection.writePlugin(arg);
        }
    }
}
