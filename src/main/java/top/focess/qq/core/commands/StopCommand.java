package top.focess.qq.core.commands;

import top.focess.qq.Main;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.util.IOHandler;

public class StopCommand extends Command {


    public StopCommand() {
        super("stop");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor(0, (sender, data, ioHandler) -> {
            ioHandler.outputLang("stop-command-stop");
            Main.exit();
            return CommandResult.ALLOW;
        });
    }

    @Override
    public void usage(CommandSender sender, IOHandler ioHandler) {
        ioHandler.output("Use: stop");
    }
}
