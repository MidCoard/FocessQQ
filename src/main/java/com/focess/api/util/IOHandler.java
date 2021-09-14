package com.focess.api.util;

import com.focess.Main;
import com.focess.listener.ConsoleListener;

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
        public boolean hasInput(boolean flag) {
            ConsoleListener.registerInputListener(this);
            while (!this.flag);
            return true;
        }
    };

    public static IOHandler getConsoleIoHandler() {
        return CONSOLE_IO_HANDLER;
    }

    public static void setConsoleIoHandler(IOHandler consoleIoHandler) {
        CONSOLE_IO_HANDLER = consoleIoHandler;
    }

    protected volatile String value = null;

    protected volatile boolean flag = false;

    public abstract void output(String output);

    public String input() {
        if (!this.flag)
            hasInput();
        this.flag = false;
        return this.value;
    }

    public void input(String input) {
        this.value = input;
        this.flag = true;
    }

    /***
     *
     * @see #hasInput(boolean)
     * @return whether there is an input string or not.
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
