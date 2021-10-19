package com.focess.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;

public class ReloginCommand extends Command {
    public ReloginCommand() {
        super("relogin");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0, (sender, dataCollection, ioHandler) -> {
            ioHandler.output("Relogining...");
            Main.relogin();
            return CommandResult.ALLOW;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: relogin");
    }
}
