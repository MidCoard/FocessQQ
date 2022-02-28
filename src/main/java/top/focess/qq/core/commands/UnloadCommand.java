package top.focess.qq.core.commands;

import top.focess.qq.Main;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.plugin.PluginClassLoader;

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
                ioHandler.outputLang("unload-command-unload-main-plugin");
                return CommandResult.REFUSE;
            }
            if (!(plugin.getClass().getClassLoader() instanceof PluginClassLoader)) {
                ioHandler.outputLang("unload-command-plugin-loader-error", plugin.getName());
                return CommandResult.REFUSE;
            }
            ioHandler.outputLang("unload-command-unload", plugin.getName());
            PluginClassLoader.disablePlugin(plugin);
            return CommandResult.ALLOW;
        }).setDataConverters(PluginDataConverter.PLUGIN_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: unload <plugin>");
    }


}
