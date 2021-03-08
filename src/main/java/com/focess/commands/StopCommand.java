package com.focess.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.focess.commands.util.ChatConstants;
import com.google.common.collect.Lists;

public class StopCommand extends Command {


    public StopCommand() {
        super("stop", Lists.newArrayList());
    }

    @Override
    public void init() {
        this.addExecutor(0, (sender, data, ioHandler)->{
            if (sender.isConsole()) {
                ioHandler.output(ChatConstants.CONSOLE_HEADER + "Start stopping...");
                Main.exit();
                return CommandResult.ALLOW;
            }
            ioHandler.output(ChatConstants.CONSOLE_HEADER + "Permission denied.");
            return CommandResult.REFUSE;
        });
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        ioHandler.output("Use: stop");
    }
}
