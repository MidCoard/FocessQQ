package com.focess.api.event.command;

import com.focess.api.command.Command;
import com.focess.api.command.CommandSender;
import com.focess.api.command.DataCollection;
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
     * The Executor
     */
    private final Command.Executor executor;

    /**
     * The args of this executor
     */
    private final DataCollection args;

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
     * @param executor the Executor
     * @param sender the executor
     * @param dataCollection the data of this executor
     * @param ioHandler the input and output handler
     */
    public CommandPrepostEvent(Command.Executor executor, CommandSender sender, DataCollection dataCollection, IOHandler ioHandler) {
        this.executor = executor;
        this.args = dataCollection;
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

    public Command.Executor getExecutor() {
        return executor;
    }

    public DataCollection getArgs() {
        return args;
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }
}
