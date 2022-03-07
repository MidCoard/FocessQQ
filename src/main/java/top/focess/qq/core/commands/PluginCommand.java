package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
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
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0,(sender,data,ioHandler)->{
            if (FocessQQ.getPlugins().size() != 0) {
                StringBuilder stringBuilder = new StringBuilder(FocessQQ.getLangConfig().get("plugin-command-list"));
                for (Plugin plugin : FocessQQ.getPlugins())
                    stringBuilder.append(' ').append(plugin.getName());
                ioHandler.output(stringBuilder.toString());
            } else ioHandler.outputLang("plugin-command-no-plugin");
            return CommandResult.ALLOW;
        },"list");
    }

    @Override
    @NotNull
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: plugin list");
    }

}
