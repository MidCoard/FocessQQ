package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.focess.core.plugin.PluginClassLoader;

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
                        ioHandler.output("Load " + file.getName());
                    else classLoader.close();
                } catch (IOException e) {
                    Main.getLogger().thr("Load Plugin Exception", e);
                    return CommandResult.REFUSE;
                }
                return CommandResult.ALLOW;
            }
            ioHandler.output("File is not existed.");
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: load <path>");
    }


}
