package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandArgument;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.PluginDataConverter;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.plugin.PluginClassLoader;

import java.util.List;

public class UnloadCommand extends Command {
    public UnloadCommand() {
        super("unload");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor( (sender, data, ioHandler) -> {
            Plugin plugin = data.getPlugin();
            if (plugin == FocessQQ.getMainPlugin()) {
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
        }, CommandArgument.of(PluginDataConverter.PLUGIN_DATA_CONVERTER));
    }

    @Override
    @NotNull
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: unload <plugin>");
    }


}
