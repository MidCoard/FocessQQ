package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandResult;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.core.listeners.ChatListener;

import java.util.List;

public class PauseCommand extends Command {

    public PauseCommand() {
        super("pause");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            ChatListener.togglePauseMode();
            ioHandler.outputLang("pause-command-pause-mode-status", ChatListener.isPauseMode());
            return CommandResult.ALLOW;
        });
    }

    @NotNull
    @Override
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: pause");
    }
}
