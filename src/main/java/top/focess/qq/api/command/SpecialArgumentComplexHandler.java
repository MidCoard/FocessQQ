package top.focess.qq.api.command;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used to handle special arguments, which start with "@"
 */
public interface SpecialArgumentComplexHandler {

    /**
     * Handle the special argument
     *
     * @param name      the name of the special argument
     * @param sender    the sender of the command
     * @param command   the command
     * @param args      the arguments of the command
     * @param i         the index of the argument
     * @param arguments the arguments of the special argument
     * @return the argument after handle
     */
    @NonNull
    String handle(String name, CommandSender sender, Command command, String[] args, int i, String... arguments);

    default void unregister() {
        CommandLine.unregister(this);
    }
}
