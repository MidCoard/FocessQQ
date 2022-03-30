package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;

import java.util.List;

public class StopCommand extends Command {


    public StopCommand() {
        super("stop");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender, data, ioHandler) -> {
            ioHandler.outputLang("stop-command-stop");
            new StopThread().start();
            return CommandResult.ALLOW;
        });
    }

    @Override
    @NotNull
    public List<String> usage(final CommandSender sender) {
        return Lists.newArrayList("Use: stop");
    }

    private static class StopThread extends Thread {

        public StopThread() {
            super("Stop-Thread");
        }

        @Override
        public void run() {
            FocessQQ.exit();
        }
    }
}
