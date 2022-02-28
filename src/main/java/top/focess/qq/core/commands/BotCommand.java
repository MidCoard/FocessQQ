package top.focess.qq.core.commands;

import top.focess.qq.Main;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.DataConverter;
import top.focess.qq.api.exceptions.BotLoginException;
import top.focess.qq.api.util.IOHandler;

public class BotCommand extends Command {

    public BotCommand() {
        super("bot");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0,(sender, dataCollection, ioHandler) -> {
            boolean flag = false;
            StringBuilder stringBuilder = new StringBuilder();
            for (Bot bot : Main.getBotManager().getBots()) {
                flag = true;
                stringBuilder.append(bot.getId()).append(',');
            }
            if (!flag)
                ioHandler.outputLang("bot-command-no-bot");
            else {

                ioHandler.output(stringBuilder.substring(0, stringBuilder.length() - 1));
            }
            return CommandResult.ALLOW;
        },"list");
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("bot-command-bot-not-exist",id);
                return CommandResult.REFUSE;
            }
            bot.login();
            ioHandler.outputLang("bot-command-login-succeed",bot.getId());
            return CommandResult.ALLOW;
        },"login").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("bot-command-bot-not-exist",id);
                return CommandResult.REFUSE;
            }
            bot.logout();
            ioHandler.outputLang("bot-command-logout-succeed",bot.getId());
            return CommandResult.ALLOW;
        },"logout").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                ioHandler.outputLang("bot-command-bot-not-exist",id);
                return CommandResult.REFUSE;
            }
            bot.relogin();
            ioHandler.outputLang("bot-command-relogin-succeed",bot.getId());
            return CommandResult.ALLOW;
        },"relogin").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(2,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Bot bot = Main.getBotManager().getBot(id);
            if (bot == null) {
                try {
                    Main.getBotManager().login(id, dataCollection.get());
                } catch (BotLoginException e) {
                    ioHandler.outputLang("bot-command-login-failed",id);
                    return CommandResult.REFUSE;
                }
                ioHandler.outputLang("bot-command-login-succeed",id);
                return CommandResult.ALLOW;
            }
            ioHandler.outputLang("bot-command-bot-exist",id);
            return CommandResult.REFUSE;
        },"login").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            long id = dataCollection.getLong();
            Main.getBotManager().remove(id);
            ioHandler.outputLang("bot-command-remove-succeed",id);
            return CommandResult.ALLOW;
        },"remove").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: bot list\n" +
                "Use: bot login <username> <password>\n" +
                "Use: bot logout <username>\n" +
                "Use: bot relogin <username>\n" +
                "Use: bot remove <username>");
    }
}
