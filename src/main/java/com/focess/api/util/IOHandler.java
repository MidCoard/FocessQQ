package com.focess.api.util;

import com.focess.Main;

import java.util.Arrays;

public abstract class IOHandler {

    /***
     * Console input and output handler
     */
    private static volatile IOHandler CONSOLE_IO_HANDLER = new IOHandler() {
        @Override
        public void output(String output) {
            String[] messages = output.split("\n");
            Arrays.stream(messages).forEachOrdered(Main.getLogger()::info);
        }

        @Override
        public String input() {
            return Main.getScanner().nextLine();
        }

        @Override
        public void input(String input) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasInput(boolean flag) {
            return Main.getScanner().hasNextLine();
        }
    };

    public static IOHandler getConsoleIoHandler() {
        return CONSOLE_IO_HANDLER;
    }

    public static void setConsoleIoHandler(IOHandler consoleIoHandler) {
        CONSOLE_IO_HANDLER = consoleIoHandler;
    }

    public abstract void output(String output);

    public abstract String input();

    public abstract void input(String input);

    /***
     *
     * @see #hasInput(boolean)
     * @return whether there is a string or not.
     */
    public boolean hasInput() {
        return hasInput(false);
    }

    /***
     *
     *
     * @param flag true indicates that this will get a toString-like str, false indicates that this will get a miraiCode-like str.
     * @return whether there is a string or not.
     */
    public abstract boolean hasInput(boolean flag);
}
