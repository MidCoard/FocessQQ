package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.plugin.PluginClassLoader;

import java.util.List;

public class DebugCommand extends Command {

    public DebugCommand() {
        super("debug");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            for (Plugin plugin : PluginClassLoader.getPlugins()) {
                if (plugin != FocessQQ.getMainPlugin())
                    CommandLine.exec("unload " + plugin.getName());
            }
            return CommandResult.ALLOW;
        });
    }

    @Override
    @NotNull
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: debug");
    }
}
