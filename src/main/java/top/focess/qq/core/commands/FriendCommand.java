package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import net.mamoe.mirai.message.code.MiraiCode;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandArgument;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.util.InputTimeoutException;

import java.util.List;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend");
    }

    @Override
    public void init() {
        this.setExecutorPermission(i -> i.isAdministrator() || i.isConsole());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            final Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("friend-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            final StringBuilder stringBuilder = new StringBuilder(FocessQQ.getLangConfig().get("friend-command-list"));
            for (final Friend friend : bot.getFriends())
                stringBuilder.append(friend.getRawName()).append("(").append(friend.getId()).append("),");
            ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"), CommandArgument.ofLong());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            final Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("friend-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            final long friendId = dataCollection.getLong();
            try {
                ioHandler.outputLang("friend-command-input-one-message");
                final String message = ioHandler.input();
                final Friend friend = bot.getFriend(friendId);
                if (friend == null) {
                    ioHandler.outputLang("friend-command-friend-not-found");
                    return CommandResult.REFUSE;
                }
                friend.sendMessage(new Message(MiraiCode.deserializeMiraiCode(message)));
            } catch (final InputTimeoutException exception) {
                ioHandler.outputLang("friend-command-input-timeout");
                return CommandResult.REFUSE;
            }
            return CommandResult.ALLOW;
        }, CommandArgument.of("send"), CommandArgument.ofLong(), CommandArgument.ofLong());
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: friend list <bot-id>"
                , "Use: friend send <bot-id> <friend-id>");
    }
}
