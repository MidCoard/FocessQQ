package com.focess.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;

public class GroupCommand extends Command {

    public GroupCommand() {
        super("group", Lists.newArrayList());
    }

    @Override
    public void init() {
        this.addExecutor(0, (sender, dataCollection, ioHandler) -> {
            if (sender.isConsole() || sender.isAuthor()) {
                StringBuilder stringBuilder = new StringBuilder("群列表: ");
                for (Group group : Main.getBot().getGroups())
                    stringBuilder.append(group.getName()).append("(").append(group.getId()).append("),");
                ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
                return CommandResult.ALLOW;
            }
            return CommandResult.REFUSE;
        }, "list");
    }

    @Override
    public void usage(CommandSender commandSender, IOHandler ioHandler) {
        if (commandSender.isConsole() || commandSender.isAuthor()) {
            ioHandler.output("Use: group list");
        }
    }
}
