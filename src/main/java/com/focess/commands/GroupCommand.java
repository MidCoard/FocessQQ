package com.focess.commands;

import com.focess.Main;
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
        this.addExecutor(0, (sender, dataCollection, ioHandler) -> {
            StringBuilder stringBuilder = new StringBuilder("群列表: ");
            for (Group group : Main.getBot().getGroups())
                stringBuilder.append(group.getName()).append("(").append(group.getId()).append("),");
            ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            return CommandResult.ALLOW;
        }, "list");
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            try {
                ioHandler.output("请输入一条消息");
                String message = ioHandler.input();
                Group group = Main.getGroup(id);
                if (group == null) {
                    ioHandler.output("未找到该群");
                    return CommandResult.REFUSE;
                }
                group.sendMessage(MiraiCode.deserializeMiraiCode(message));
            } catch (InputTimeoutException exception) {
                ioHandler.output("输入超时");
                return CommandResult.REFUSE;
            }
            return CommandResult.ALLOW;
        },"send").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: group list\n" + "Use: group send <group>");
    }
}
