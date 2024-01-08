package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.BotLoginException;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;

import java.util.List;

public class BotCommand extends Command {

    public BotCommand() {
        super("bot");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            boolean flag = false;
            final StringBuilder stringBuilder = new StringBuilder();
            for (final Bot bot : FocessQQ.getBotManager().getBots()) {
                flag = true;
                stringBuilder.append(bot.getId()).append(',');
            }
            if (!flag)
                ioHandler.outputLang("bot-command-no-bot");
            else
                ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"));
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            final Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("bot-command-bot-not-exist", id);
                return CommandResult.REFUSE;
            }
            try {
                bot.login();
            } catch (final BotLoginException e) {
                ioHandler.outputLang("bot-command-login-failed", id);
                FocessQQ.getLogger().thrLang("bot-command-login-failed", e);
                return CommandResult.REFUSE;
            }
            ioHandler.outputLang("bot-command-login-succeed", bot.getId());
            return CommandResult.ALLOW;
        }, CommandArgument.of("login"), CommandArgument.ofLong());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            final Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("bot-command-bot-not-exist", id);
                return CommandResult.REFUSE;
            }
            bot.logout();
            ioHandler.outputLang("bot-command-logout-succeed", bot.getId());
            return CommandResult.ALLOW;
        }, CommandArgument.of("logout"), CommandArgument.ofLong());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            final Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("bot-command-bot-not-exist", id);
                return CommandResult.REFUSE;
            }
            try {
                if (bot.relogin())
                    ioHandler.outputLang("bot-command-relogin-succeed", bot.getId());
                else {
                    ioHandler.outputLang("bot-command-relogin-failed", bot.getId());
                    return CommandResult.REFUSE;
                }
            } catch (final BotLoginException e) {
                ioHandler.outputLang("bot-command-relogin-failed", bot.getId());
                FocessQQ.getLogger().thrLang("bot-command-relogin-failed", e);
                return CommandResult.REFUSE;
            }
            return CommandResult.ALLOW;
        }, CommandArgument.of("relogin"), CommandArgument.ofLong());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            final Bot bot = FocessQQ.getBotManager().getBot(id);
            if (bot == null) {
                FocessQQ.getBotManager().login(id, dataCollection.get(), FocessQQ.getMainPlugin());
                ioHandler.outputLang("bot-command-login-succeed", id);
                return CommandResult.ALLOW;
            }
            ioHandler.outputLang("bot-command-bot-exist", id);
            return CommandResult.REFUSE;
        }, CommandArgument.of("login"), CommandArgument.ofLong(), CommandArgument.ofString());
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            final long id = dataCollection.getLong();
            FocessQQ.getBotManager().remove(id);
            ioHandler.outputLang("bot-command-remove-succeed", id);
            return CommandResult.ALLOW;
        }, CommandArgument.of("remove"), CommandArgument.ofLong());
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: bot list",
                "Use: bot login <bot-id> <password>",
                "Use: bot login <bot-id>",
                "Use: bot logout <bot-id>",
                "Use: bot relogin <bot-id>",
                "Use: bot remove <bot-id>");
    }

}
