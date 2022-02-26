package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;

public class StopCommand extends Command {


    public StopCommand() {
        super("stop");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0, (sender, data, ioHandler) -> {
            ioHandler.outputLang("stop-command-stop");
            Main.exit();
            return CommandResult.ALLOW;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: stop");
    }
}
