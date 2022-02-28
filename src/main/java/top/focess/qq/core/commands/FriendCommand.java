package top.focess.qq.core.commands;

import top.focess.qq.Main;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.DataConverter;
import top.focess.qq.api.exceptions.InputTimeoutException;
import top.focess.qq.api.util.IOHandler;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.code.MiraiCode;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(1, (sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("friend-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            StringBuilder stringBuilder = new StringBuilder(Main.getLangConfig().get("friend-command-list"));
            for (Friend friend : bot.getFriends())
                stringBuilder.append(friend.getNick()).append("(").append(friend.getId()).append("),");
            ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            return CommandResult.ALLOW;
        }, "list").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(2,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("friend-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            long friendId = dataCollection.getLong();
            try {
                ioHandler.outputLang("friend-command-input-one-message");
                String message = ioHandler.input();
                Friend friend = bot.getFriend(friendId);
                if (friend == null) {
                    ioHandler.outputLang("friend-command-friend-not-found");
                    return CommandResult.REFUSE;
                }
                friend.sendMessage(MiraiCode.deserializeMiraiCode(message));
            } catch (InputTimeoutException exception) {
                ioHandler.outputLang("friend-command-input-timeout");
                return CommandResult.REFUSE;
            }
            return CommandResult.ALLOW;
        },"send").setDataConverters(DataConverter.LONG_DATA_CONVERTER,DataConverter.LONG_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: friend list <username>\n" + "Use: friend send <username> <friend>");
    }
}
