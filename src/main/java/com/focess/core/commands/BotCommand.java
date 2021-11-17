package com.focess.core.commands;

import com.focess.Main;
import com.focess.api.bot.Bot;
import com.focess.api.command.Command;
import com.focess.api.command.CommandResult;
import com.focess.api.command.CommandSender;
import com.focess.api.command.DataConverter;
import com.focess.api.exceptions.BotLoginException;
import com.focess.api.util.IOHandler;

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
                ioHandler.output("There is no bots");
            else
                ioHandler.output(stringBuilder.substring(0,stringBuilder.length() - 1));
            return CommandResult.ALLOW;
        },"list");
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            Bot bot = Main.getBotManager().getBot(dataCollection.getLong());
            if (bot == null) {
                ioHandler.output("Bot id does not exist");
                return CommandResult.REFUSE;
            }
            bot.login();
            ioHandler.output("Bot log in successfully");
            return CommandResult.ALLOW;
        },"login").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            Bot bot = Main.getBotManager().getBot(dataCollection.getLong());
            if (bot == null) {
                ioHandler.output("Bot id does not exist");
                return CommandResult.REFUSE;
            }
            bot.logout();
            ioHandler.output("Bot log out successfully");
            return CommandResult.ALLOW;
        },"logout").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            Bot bot = Main.getBotManager().getBot(dataCollection.getLong());
            if (bot == null) {
                ioHandler.output("Bot id does not exist");
                return CommandResult.REFUSE;
            }
            bot.relogin();
            ioHandler.output("Bot relogin successfully");
            return CommandResult.ALLOW;
        },"relogin").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(2,(sender, dataCollection, ioHandler) -> {
            Bot bot = Main.getBotManager().getBot(dataCollection.getLong());
            if (bot == null) {
                try {
                    Main.getBotManager().login(dataCollection.getLong(), dataCollection.get());
                } catch (BotLoginException e) {
                    ioHandler.output("Bot log in failed");
                    return CommandResult.REFUSE;
                }
                ioHandler.output("Bot log in successfully");
                return CommandResult.ALLOW;
            }
            ioHandler.output("Bot does exist");
            return CommandResult.REFUSE;
        },"login").setDataConverters(DataConverter.LONG_DATA_CONVERTER);
        this.addExecutor(1,(sender, dataCollection, ioHandler) -> {
            Main.getBotManager().remove(dataCollection.getLong());
            ioHandler.output("Remove successfully");
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
