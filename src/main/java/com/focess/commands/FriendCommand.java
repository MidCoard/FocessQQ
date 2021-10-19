package com.focess.commands;

import com.focess.Main;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.command.DataConverter;
import com.focess.api.exceptions.InputTimeoutException;
import com.focess.api.util.IOHandler;
import com.google.common.collect.Lists;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.code.MiraiCode;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0, (sender, dataCollection, ioHandler) -> {
            StringBuilder stringBuilder = new StringBuilder("朋友列表: ");
            for (Friend friend : Main.getBot().getFriends())
                stringBuilder.append(friend.getNick()).append("(").append(friend.getId()).append("),");
            ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            return CommandResult.ALLOW;
        }, "list");
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            try {
                ioHandler.output("请输入一条消息");
                String message = ioHandler.input();
                Friend friend = Main.getFriend(id);
                if (friend == null) {
                    ioHandler.output("未找到该朋友");
                    return CommandResult.REFUSE;
                }
                friend.sendMessage(MiraiCode.deserializeMiraiCode(message));
            } catch (InputTimeoutException exception) {
                ioHandler.output("输入超时");
                return CommandResult.REFUSE;
            }
            return CommandResult.ALLOW;
        },"send").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: friend list\n" + "Use: friend send <friend>");
    }
}
