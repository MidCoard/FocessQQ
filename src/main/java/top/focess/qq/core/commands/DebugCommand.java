package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandResult;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;

import java.util.List;

public class DebugCommand extends Command {

    public DebugCommand() {
        super("debug");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            FocessQQ.getLogger().toggleDebugOutput();
            ioHandler.outputLang("debug-command-debug-output-status",FocessQQ.getLogger().isDebugOutput());
            return CommandResult.ALLOW;
        });
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: debug");
    }
}
