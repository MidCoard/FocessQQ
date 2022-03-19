package top.focess.qq.core.commands.special;

import org.checkerframework.checker.nullness.qual.NonNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.SpecialArgumentHandler;

public class TargetIdArgumentHandler implements SpecialArgumentHandler {
    @Override
    public @NonNull String handle(CommandSender sender, Command command, String[] args, int i) {
        if (sender.isFriend())
            return String.valueOf(FocessQQ.getBot().getId());
        if (sender.isMember())
            return String.valueOf(sender.getMember().getGroup().getId());
        return "";
    }
}
