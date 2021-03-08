package com.focess.api.util;

import com.focess.Main;
import com.focess.api.command.CommandSender;

public abstract class IOHandler {

    public static IOHandler getIoHandler() {
        return IO_HANDLER;
    }

    public static volatile IOHandler IO_HANDLER = new IOHandler() {
        @Override
        public void output(String output) {
            System.out.println(output);
        }

        @Override
        public String input() {
            return Main.scanner.nextLine();
        }

        @Override
        public <T> void handle(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasInput(boolean flag) {
            return Main.scanner.hasNextLine();
        }
    };

    public static void setIoHandler(IOHandler ioHandler) {
        IO_HANDLER = ioHandler;
    }

    public static IOHandler getIoHandlerByCommandSender(CommandSender commandSender) {
        return new IOHandler() {
            public boolean hasSent() {
                return hasSent;
            }

            @Override
            public void output(String output) {
                if (commandSender.isMember())
                    commandSender.getMember().getGroup().sendMessage(output);
                else if (commandSender.isFriend())
                    commandSender.getFriend().sendMessage(output);
                else IO_HANDLER.output(output);
            }

            @Override
            public String input() {
                if (commandSender.isConsole())
                    return IO_HANDLER.input();
                return this.waitForInput();
            }

            @Override
            public <T> void handle(T t) {
                this.sentString = (String) t;
                this.hasSent = true;
            }

            @Override
            public boolean hasInput(boolean flag) {
                Main.registerIOHandler(this,commandSender,flag);
                while (!hasSent());
                return this.sentString != null;
            }

            private volatile String sentString = null;

            private volatile boolean hasSent = false;

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

    public abstract <T> void handle(T  t);

    public boolean hasInput() {
        return hasInput(false);
    }

    public abstract boolean hasInput(boolean flag);
}
