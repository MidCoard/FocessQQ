package top.focess.qq.api.util;

import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.core.listeners.ConsoleListener;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

import java.util.Arrays;

/**
 * This class is used to handle input and output when executing Command.
 */
public abstract class IOHandler extends top.focess.command.IOHandler {

    /**
     * Console input and output handler
     */
    private static volatile IOHandler CONSOLE_IO_HANDLER = new IOHandler() {

        @Override
        public void output(@NotNull final String output) {
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

    public static IOHandler getConsoleIoHandler() {
        return CONSOLE_IO_HANDLER;
    }

    public static void setConsoleIoHandler(final IOHandler consoleIoHandler) {
        CONSOLE_IO_HANDLER = consoleIoHandler;
    }

    /**
     * Used to output formatted language key
     *
     * @param key     the language key
     * @param objects the objects need to replace
     */
    public void outputLang(final String key, final Object... objects) {
        this.output(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

}
