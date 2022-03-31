package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandArgument;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;

public class PluginCommand extends Command {

    public PluginCommand() {
        super("plugin");
    }

    @Override
    public void init() {
        this.setExecutorPermission(i -> i.isAdministrator() || i.isConsole());
        this.addExecutor((sender, data, ioHandler) -> {
            if (FocessQQ.getPlugins().size() != 0) {
                final StringBuilder stringBuilder = new StringBuilder(FocessQQ.getLangConfig().get("plugin-command-list"));
                for (final Plugin plugin : FocessQQ.getPlugins())
                    stringBuilder.append(' ').append(plugin.getName());
                ioHandler.output(stringBuilder.toString());
            } else ioHandler.outputLang("plugin-command-no-plugin");
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"));
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: plugin list");
    }

}
