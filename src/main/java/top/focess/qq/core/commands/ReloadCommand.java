package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.plugin.PluginClassLoader;

import java.io.File;
import java.util.List;

public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload");
    }

    public static boolean reloadPlugin(@NotNull final Plugin plugin) {
        FocessQQ.getLogger().debugLang("start-reload-plugin", plugin.getName());
        if (plugin == FocessQQ.getMainPlugin())
            return false;
        final File pluginFile = PluginClassLoader.disablePlugin(plugin);
        if (pluginFile == null) {
            FocessQQ.getLogger().fatalLang("fatal-reload-plugin", plugin.getName());
            return false;
        }
        final PluginClassLoader classLoader;
        try {
            classLoader = new PluginClassLoader(pluginFile);
            if (classLoader.load())
                return true;
            else {
                classLoader.close();
                return false;
            }
        } catch (final Exception e) {
            FocessQQ.getLogger().thrLang("exception-reload-plugin", e);
            return false;
        }
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, data, ioHandler) -> {
            final Plugin plugin = data.get(Plugin.class);
            if (plugin == FocessQQ.getMainPlugin()) {
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
        }, CommandArgument.of(PluginDataConverter.PLUGIN_DATA_CONVERTER));
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: reload <plugin>");
    }
}
