package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.plugin.Plugin;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.command.converter.PluginDataConverter;
import com.focess.api.util.IOHandler;
import com.focess.core.plugin.PluginClassLoader;

import java.io.File;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload");
    }

    public static boolean reloadPlugin(Plugin plugin) {
        if (plugin == Main.getMainPlugin())
            return false;
        File pluginFile = PluginClassLoader.disablePlugin(plugin);
        PluginClassLoader classLoader;
        try {
            classLoader = new PluginClassLoader(pluginFile);
            if (classLoader.load())
                return true;
            else {
                classLoader.close();
                return false;
            }
        } catch (Exception e) {
            Main.getLogger().thr("Plugin Reload Exception", e);
            return false;
        }
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, data, ioHandler) -> {
            Plugin plugin = data.getPlugin();
            if (plugin == Main.getMainPlugin()) {
                ioHandler.output("Can't reload the Main Plugin, if you want to stop the server use stop command.");
                return CommandResult.REFUSE;
            }
            if (!(plugin.getClass().getClassLoader() instanceof PluginClassLoader)) {
                ioHandler.output("Plugin " + plugin.getName() + " is not loaded from PluginClassLoader");
                return CommandResult.REFUSE;
            }
            ioHandler.output("Reload " + plugin.getName());
            if (reloadPlugin(plugin)) {
                ioHandler.output("Reload " + plugin.getName() + " success");
            } else ioHandler.output("Reload " + plugin.getName() + " failed");
            return CommandResult.ALLOW;
        }).setDataConverters(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: reload <plugin>");
    }
}
