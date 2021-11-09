package com.focess.api.event.command;

import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.event.Cancellable;
import com.focess.api.event.Event;
import com.focess.api.event.ListenerHandler;
import com.focess.api.util.IOHandler;

/**
 * Called before command executing
 */
public class CommandPrepostEvent extends Event implements Cancellable {

    private static final ListenerHandler LISTENER_HANDLER = new ListenerHandler();

    /**
     * The command
     */
    private final Command command;
    /**
     * The args of this executor
     */
    private final String[] args;

    /**
     * The input and output handler
     */
    private final IOHandler ioHandler;

    /**
     * The executor
     */
    private final CommandSender sender;

    /**
     * Indicate this event is cancelled
     */
    private boolean cancelled;

    /**
     * Constructs a CommandPrepostEvent
     * @param sender the executor
     * @param command the Command
     * @param args the data of this executor
     * @param ioHandler the input and output handler
     */
    public CommandPrepostEvent(CommandSender sender, Command command, String[] args, IOHandler ioHandler) {
        this.command = command;
        this.args = args;
        this.ioHandler = ioHandler;
        this.cancelled = false;
        this.sender = sender;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String[] getArgs() {
        return args;
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Command getCommand() {
        return command;
    }
}
