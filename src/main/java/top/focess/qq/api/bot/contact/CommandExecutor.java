package top.focess.qq.api.bot.contact;

import top.focess.qq.api.command.CommandSender;

public interface CommandExecutor {

    default CommandSender getCommandSender() {
        return CommandSender.of(this);
    }
}
