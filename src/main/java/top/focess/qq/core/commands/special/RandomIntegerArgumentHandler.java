package top.focess.qq.core.commands.special;

import org.checkerframework.checker.nullness.qual.NonNull;
import top.focess.qq.api.command.Command;
import top.focess.qq.api.command.CommandSender;
import top.focess.qq.api.command.SpecialArgumentHandler;

import java.util.Random;

public class RandomIntegerArgumentHandler implements SpecialArgumentHandler {


    private static final Random RANDOM = new Random();

    @Override
    public @NonNull String handle(final CommandSender sender, final Command command, final String[] args, final int i) {
        return String.valueOf(RANDOM.nextInt());
    }
}
