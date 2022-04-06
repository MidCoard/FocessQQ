package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.core.plugin.PluginClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LoadCommand extends Command {

    public LoadCommand() {
        super("load");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, data, ioHandler) -> {
            final String path = data.get();
            final File file = new File(path);
            if (file.exists() && file.getName().endsWith(".jar")) {
                try {
                    final PluginClassLoader classLoader = new PluginClassLoader(file);
                    if (classLoader.load())
                        ioHandler.outputLang("load-command-load-succeed", classLoader.getPlugin().getName());
                    else {
                        ioHandler.outputLang("load-command-load-failed", file.getName());
                        classLoader.close();
                    }
                } catch (final IOException e) {
                    FocessQQ.getLogger().thrLang("exception-load-plugin", e);
                    return CommandResult.REFUSE;
                }
                return CommandResult.ALLOW;
            }
            ioHandler.outputLang("load-command-file-not-exist", path);
            return CommandResult.REFUSE;
        }, CommandArgument.ofString());
    }

    @NotNull
    @Override
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: load <file>");
    }


}
