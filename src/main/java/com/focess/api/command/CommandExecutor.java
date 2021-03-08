package com.focess.api.command;


import com.focess.api.util.IOHandler;

public interface CommandExecutor {
    CommandResult execute(CommandSender sender, DataCollection dataCollection, IOHandler ioHandler);

}
