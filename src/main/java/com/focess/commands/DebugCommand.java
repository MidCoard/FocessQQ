package com.focess.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;

public class DebugCommand extends Command {

    private static boolean debug = false;

    public DebugCommand() {
        super("debug", Lists.newArrayList());
    }

    @Override
    public void init() {
        this.addExecutor(0, (sender, dataCollection, ioHandler) -> {
            if (sender.isConsole()) {
                debug = !debug;
                ioHandler.output("DebugStatus :" + debug);
                Main.setDebug(debug);
                return CommandResult.ALLOW;
            }
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        if (commandSender.isConsole())
            ioHandler.output("Use: debug");
    }
}
