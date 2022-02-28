package top.focess.qq.core.commands;

import top.focess.qq.Main;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.IOHandler;

public class PluginCommand extends Command {

    public PluginCommand() {
        super("plugin");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0,(sender,data,ioHandler)->{
            if (Main.getPlugins().size() != 0) {
                StringBuilder stringBuilder = new StringBuilder(Main.getLangConfig().get("plugin-command-list"));
                for (Plugin plugin : Main.getPlugins())
                    stringBuilder.append(' ').append(plugin.getName());
                ioHandler.output(stringBuilder.toString());
            } else ioHandler.outputLang("plugin-command-no-plugin");
            return CommandResult.ALLOW;
        },"list");
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        ioHandler.output("Use: plugin list");
    }
}
