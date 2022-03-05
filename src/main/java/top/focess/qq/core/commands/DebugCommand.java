package top.focess.qq.core.commands;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.plugin.PluginClassLoader;

public class DebugCommand extends Command {

    public DebugCommand() {
        super("debug");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0,(sender, dataCollection, ioHandler) -> {
            for (Plugin plugin : PluginClassLoader.getPlugins()) {
                if (plugin != FocessQQ.getMainPlugin())
                    FocessQQ.CommandLine.exec("unload " + plugin.getName());
                if (plugin.getName().contains("SICE"))
                    System.out.println(plugin.getClass().getClassLoader().getParent());
            }
            return CommandResult.ALLOW;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: debug");
    }
}
