package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import net.mamoe.mirai.message.code.MiraiCode;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Group;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandArgument;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.util.InputTimeoutException;

import java.util.List;

public class GroupCommand extends Command {

    public GroupCommand() {
        super("group");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor( (sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("group-command-bot-not-found", id);
                return CommandResult.REFUSE;
            }
            if (!bot.getGroups().isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder(FocessQQ.getLangConfig().get("group-command-list"));
                for (Group group : bot.getGroups())
                    stringBuilder.append(group.getName()).append("(").append(group.getId()).append("),");
                ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            } else ioHandler.outputLang("group-command-no-group");
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"),CommandArgument.ofLong());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = FocessQQ.getBotManager().getBot(id);
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
        },CommandArgument.of("send"),CommandArgument.ofLong(),CommandArgument.ofLong());
    }

    @NotNull
    @Override
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: group list <bot-id>",
                "Use: group send <bot-id> <group-id>");
    }
}
