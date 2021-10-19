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
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, data, ioHandler) -> {
            Plugin plugin = data.getPlugin();
            ioHandler.output("Unload " + plugin.getName());
            if (!(plugin.getClass().getClassLoader() instanceof LoadCommand.PluginClassLoader))
                Main.getLogger().debug("Plugin " + plugin.getName() + " is not loaded from PluginClassLoader");
            LoadCommand.disablePlugin(plugin);
            return CommandResult.ALLOW;
        }).setDataConverters(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: unload [plugin-name]");
    }

    /**
     * Convert the String argument to Plugin argument
     */
    public static class PluginDataConverter extends DataConverter<Plugin> {

        /**
         * Convert the String argument to Plugin argument
         */
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
