package com.focess.api.util;

import com.focess.Main;
import com.focess.listener.ConsoleListener;

import java.util.Arrays;

/**
 * This class is used to handle input and output when executing Command.
 */
public abstract class IOHandler {

    /**
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

    /**
     * Used to output String
     *
     * @param output output String
     */
    public abstract void output(String output);

    /**
     * Used to get input String
     *
     * @return the input String
     */
    public String input() {
        if (!this.flag)
            hasInput();
        this.flag = false;
        return this.value;
    }

    /**
     * Used to input String
     *
     * @param input the inputted String
     */
    public void input(String input) {
        this.value = input;
        this.flag = true;
    }

    /**
     * Indicate there needs the MiraiCode of this input if it is a Mirai Message, or the string value of this input.
     *
     * @see #hasInput(boolean)
     * @return true if there is an input String, false otherwise
     */
    public boolean hasInput() {
        return hasInput(false);
    }

    /**
     * Indicate there needs an input String.
     *
     * @param flag true if you need the MiraiCode of this input when it is a Mirai Message, false if you need the string value of this input
     * @return true if there is an input String, false otherwise
     */
    public abstract boolean hasInput(boolean flag);
}
