package top.focess.qq.api.util;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.FocessQQ;
import top.focess.qq.core.listeners.ConsoleListener;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

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
        public void output(final String output) {
            final String[] messages = output.split("\n");
            Arrays.stream(messages).forEachOrdered(FocessQQ.getLogger()::info);
        }

        @Override
        public boolean hasInput(final boolean flag) {
            ConsoleListener.registerInputListener(this);
            while (!this.flag) ;
            return true;
        }
    };
    @Nullable
    protected volatile String value;
    protected volatile boolean flag;

    public static IOHandler getConsoleIoHandler() {
        return CONSOLE_IO_HANDLER;
    }

    public static void setConsoleIoHandler(final IOHandler consoleIoHandler) {
        CONSOLE_IO_HANDLER = consoleIoHandler;
    }

    /**
     * Used to output String
     *
     * @param output output String
     */
    public abstract void output(String output);

    /**
     * Used to output formatted language key
     *
     * @param key     the language key
     * @param objects the objects need to replace
     */
    public void outputLang(final String key, final Object... objects) {
        this.output(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Used to get input String
     *
     * @return the input String
     * @throws InputTimeoutException if the command has waited for more than 10 minutes to get executor input string
     */
    public String input() throws InputTimeoutException {
        if (!this.flag)
            this.hasInput();
        this.flag = false;
        if (this.value == null)
            throw new InputTimeoutException();
        return this.value;
    }

    /**
     * Used to input String
     *
     * @param input the inputted String
     */
    public void input(@Nullable final String input) {
        this.value = input;
        this.flag = true;
    }

    /**
     * Indicate there needs the MiraiCode of this input if it is a Mirai Message, or the string value of this input.
     *
     * @return true if there is an input String, false otherwise
     * @see #hasInput(boolean)
     */
    public boolean hasInput() {
        return this.hasInput(false);
    }

    /**
     * Indicate there needs an input String.
     *
     * @param flag true if you need the MiraiCode of this input when it is a Mirai Message, false if you need the string value of this input
     * @return true if there is an input String, false otherwise
     */
    public abstract boolean hasInput(boolean flag);

}
