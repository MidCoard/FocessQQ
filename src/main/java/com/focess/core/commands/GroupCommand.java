package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.bot.Bot;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.command.DataConverter;
import com.focess.api.exceptions.InputTimeoutException;
import com.focess.api.util.IOHandler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;

public class GroupCommand extends Command {

    public GroupCommand() {
        super("group");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("group-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            if (!bot.getGroups().isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder(Main.getLangConfig().get("group-command-list"));
                for (Group group : bot.getGroups())
                    stringBuilder.append(group.getName()).append("(").append(group.getId()).append("),");
                ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            } else ioHandler.outputLang("group-command-no-group");
            return CommandResult.ALLOW;
        }, "list").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(2,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("group-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            long groupId = dataCollection.getLong();
            try {
                ioHandler.outputLang("group-command-input-one-message");
                String message = ioHandler.input();
                Group group = bot.getGroup(groupId);
                if (group == null) {
                    ioHandler.outputLang("group-command-group-not-found", groupId);
                    return CommandResult.REFUSE;
                }
                group.sendMessage(MiraiCode.deserializeMiraiCode(message));
            } catch (InputTimeoutException exception) {
                ioHandler.outputLang("group-command-input-timeout");
                return CommandResult.REFUSE;
            }
            return CommandResult.ALLOW;
        },"send").setDataConverters(DataConverter.LONG_DATA_CONVERTER,DataConverter.LONG_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: group list <username>\n" + "Use: group send <username> <group>");
    }
}
