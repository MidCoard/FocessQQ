package top.focess.qq.core.commands;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import top.focess.command.CommandArgument;
import top.focess.command.CommandResult;
import top.focess.command.DataConverter;
import top.focess.command.InputTimeoutException;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandLine;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");
    }

    @Override
    public void init() {
        this.setExecutorPermission(i->i.isConsole() || i.isAdministrator());
        this.addExecutor((sender,dataCollection,ioHandler)->{
            for (Plugin plugin : Plugin.getPlugins())
                if (plugin != FocessQQ.getMainPlugin())
                    CommandLine.exec("unload " + plugin.getName());
            ioHandler.output("unload all plugins");
            return CommandResult.ALLOW;
        } );
        this.addExecutor((sender, dataCollection, ioHandler) -> {
            ioHandler.output("please input one message");
            try {
                String input = ioHandler.input();
                ioHandler.output("you input: " + input);
                ioHandler.output("please input one integer");
                Integer input2 = ioHandler.input(DataConverter.INTEGER_DATA_CONVERTER);
                if (input2 == null)
                    ioHandler.output("input error");
                else {
                    ioHandler.output("you input plus 1: " + (input2 + 1));
                    ioHandler.output("please input one message");
                    if (ioHandler.hasInput()) {
                        ioHandler.output("you have input");
                        ioHandler.output("You input: " + ioHandler.input());
                        ioHandler.output("please input one message in 10 seconds");
                        ioHandler.hasInput(true, 10);
                        try {
                            ioHandler.output("You input: " + ioHandler.input());
                        } catch (InputTimeoutException e) {
                            ioHandler.output("input timeout for 10 seconds");
                        }
                    }
                }
            } catch (InputTimeoutException e) {
                ioHandler.output("input timeout");
            }
            return CommandResult.ALLOW;
        }, CommandArgument.of("input"));
    }

    @NotNull
    @Override
    public List<String> usage(CommandSender sender) {
        return Lists.newArrayList("Use: test");
    }
}
