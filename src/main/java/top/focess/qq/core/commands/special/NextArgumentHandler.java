package top.focess.qq.core.commands.special;

import org.checkerframework.checker.nullness.qual.NonNull;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.SpecialArgumentHandler;

public class NextArgumentHandler implements SpecialArgumentHandler {
    @Override
    public @NonNull String handle(final CommandSender sender, final Command command, final String[] args, final int i) {
        if (i + 1 < args.length)
            return args[i + 1];
        return "";
    }
}
