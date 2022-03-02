package top.focess.qq.core.commands;

import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.util.IOHandler;
import top.focess.qq.core.plugin.PluginClassLoader;

import java.io.File;
import java.io.IOException;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, data, ioHandler) -> {
            String path = data.get();
            File file = new File(path);
            if (file.exists() && file.getName().endsWith(".jar")) {
                try {
                    PluginClassLoader classLoader = new PluginClassLoader(file);
                    if (classLoader.load())
                        ioHandler.outputLang("load-command-load-succeed", classLoader.getPlugin().getName());
                    else {
                        ioHandler.outputLang("load-command-load-failed", file.getName());
                        classLoader.close();
                    }
                } catch (IOException e) {
                    FocessQQ.getLogger().thrLang("exception-load-plugin", e);
                    return CommandResult.REFUSE;
                }
                return CommandResult.ALLOW;
            }
            ioHandler.outputLang("load-command-file-not-exist", path);
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: load <path>");
    }


}
