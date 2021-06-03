package com.focess.api.util;

import com.focess.Main;
import com.focess.api.command.CommandSender;

public abstract class IOHandler {

    /***
     * Console CommandSender output and system output
     */
    @Deprecated
    public static volatile IOHandler IO_HANDLER = new IOHandler() {
        @Override
        public void output(String output) {
            System.out.println(output);
        }

        @Override
        public String input() {
            return Main.getScanner().nextLine();
        }

        @Override
        public <T> void handle(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasInput(boolean flag) {
            return Main.getScanner().hasNextLine();
        }
    };

    public static IOHandler getIoHandler() {
        return IO_HANDLER;
    }

    public static void setIoHandler(IOHandler ioHandler) {
        IO_HANDLER = ioHandler;
    }

    public static IOHandler getIoHandlerByCommandSender(CommandSender commandSender) {
        if (commandSender.isConsole())
            return getIoHandler();
        return new IOHandler() {
            private volatile String sentString = null;
            private volatile boolean hasSent = false;

            public boolean hasSent() {
                return hasSent;
            }

            @Override
            public void output(String output) {
                if (commandSender.isMember())
                    commandSender.getMember().getGroup().sendMessage(output);
                else if (commandSender.isFriend())
                    commandSender.getFriend().sendMessage(output);
            }

            @Override
            public String input() {
                return this.waitForInput();
            }

            @Override
            public <T> void handle(T t) {
                try {
                    this.sentString = (String) t;
                } catch (ClassCastException e) {
                    this.sentString = null;
                }
                this.hasSent = true;
            }

            @Override
            public boolean hasInput(boolean flag) {
                Main.registerIOHandler(this, commandSender, flag);
                while (!hasSent()) ;
                return this.sentString != null;
            }

            private String waitForInput() {
                if (!this.hasSent())
                    hasInput();
                this.hasSent = false;
                return this.sentString;
            }

        };
    }

    public abstract void output(String output);

    public abstract String input();

    public abstract <T> void handle(T t);

    public boolean hasInput() {
        return hasInput(false);
    }

    public abstract boolean hasInput(boolean flag);
}
