package top.focess.qq.core.commands;

import top.focess.qq.Main;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.plugin.PluginClassLoader;

import java.io.File;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload");
    }

    public static boolean reloadPlugin(Plugin plugin) {
        Main.getLogger().debugLang("start-reload-plugin", plugin.getName());
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
            Main.getLogger().thrLang("exception-reload-plugin", e);
            return false;
        }
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, data, ioHandler) -> {
            Plugin plugin = data.getPlugin();
            if (plugin == Main.getMainPlugin()) {
                ioHandler.outputLang("reload-command-reload-main-plugin");
                return CommandResult.REFUSE;
            }
            if (!(plugin.getClass().getClassLoader() instanceof PluginClassLoader)) {
                ioHandler.outputLang("reload-command-plugin-loader-error", plugin.getName());
                return CommandResult.REFUSE;
            }
            if (reloadPlugin(plugin))
                ioHandler.outputLang("reload-command-reload-plugin-succeed", plugin.getName());
            else ioHandler.outputLang("reload-command-reload-plugin-failed", plugin.getName());
            return CommandResult.ALLOW;
        }).setDataConverters(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: reload <plugin>");
    }
}
