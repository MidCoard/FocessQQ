package com.focess.commands;

import com.focess.api.Plugin;
import com.focess.api.command.*;
import com.focess.api.util.IOHandler;
import com.focess.commands.util.ChatConstants;
import com.google.common.collect.Lists;

public class UnloadCommand extends Command {
    public UnloadCommand() {
        super("unload", Lists.newArrayList());
    }

    @Override
    public void init() {
        this.addExecutor(1, (sender, data, ioHandler)->{
            if (sender.isConsole()) {
                ioHandler.output(ChatConstants.CONSOLE_HEADER + "Start unloading...");
                Plugin plugin = data.getPlugin();
                if (plugin.getClass().getClassLoader() instanceof LoadCommand.PluginClassLoader) {
                    ioHandler.output(ChatConstants.CONSOLE_HEADER + "Disable " + plugin.getName());
                    LoadCommand.disablePlugin(plugin);
                    System.gc();
                    ioHandler.output(ChatConstants.CONSOLE_HEADER + "End unloading...");
                    return CommandResult.ALLOW;
                }
                ioHandler.output(ChatConstants.CONSOLE_HEADER + "End unloading...");
                return CommandResult.REFUSE;
            }
            return CommandResult.REFUSE;
        }).addDataConverter(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
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
