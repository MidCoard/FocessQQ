package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandArgument;
import top.focess.qq.api.command.CommandResult;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.converter.CommandDataConverter;

import java.util.List;


public class CommandCommand extends Command {

    public CommandCommand() {
        super("command");
    }

    @Override
    public void init() {
        this.setExecutorPermission(CommandSender::isConsole);
        this.addExecutor((sender,data,ioHandler)->{
            if (Command.getCommands().size() != 0 ) {
                StringBuilder stringBuilder = new StringBuilder(FocessQQ.getLangConfig().get("command-command-list"));
                for (Command command : Command.getCommands())
                    stringBuilder.append(' ').append(command.getName());
                ioHandler.output(stringBuilder.toString());
            } else ioHandler.outputLang("command-command-no-command");
            return CommandResult.ALLOW;
        }, CommandArgument.of("list"));
        this.addExecutor((sender,data,ioHandler) ->{
            Command command = data.getCommand();
            if (command.getPlugin() == FocessQQ.getMainPlugin()) {
                ioHandler.outputLang("command-command-unload-main-plugin-command", command.getName());
                return CommandResult.REFUSE;
            }
            command.unregister();
            ioHandler.outputLang("command-command-unload",command.getName());
            return CommandResult.ALLOW;
        },CommandArgument.of("unload"),CommandArgument.of(CommandDataConverter.COMMAND_DATA_CONVERTER));
    }

    @Override
    @NotNull
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList(
                "Use: command list",
                "Use: command unload <command>"
        );
    }
}
