package top.focess.qq.api.command;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used to handle special arguments, which start with "@"
 */
public interface SpecialArgumentHandler extends SpecialArgumentComplexHandler {

    /**
     * Handle the special argument
     *
     * @param sender  the sender of the command
     * @param command the command
     * @param args    the arguments
     * @param i       the index of the argument
     * @return the argument after handle
     */
    @NonNull
    String handle(CommandSender sender, Command command, String[] args, int i);

    @Override
    @NonNull
    default String handle(final String name, final CommandSender sender, final Command command, final String[] args, final int i, final String... arguments) {
        return this.handle(sender, command, args, i);
    }
}
