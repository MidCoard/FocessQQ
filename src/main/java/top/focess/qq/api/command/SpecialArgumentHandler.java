package top.focess.qq.api.command;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used to handle special arguments, which start with "@"
 */
public interface SpecialArgumentHandler {

    /**
     * Handle the special argument
     * @param sender the sender of the command
     * @param command the command
     * @param args the arguments
     * @param i the index of the argument
     * @return the argument after handle
     */
    @NonNull
    String handle(CommandSender sender, Command command, String[] args, int i);
}
