package top.focess.qq.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.command.DataConverter;
import top.focess.command.InputTimeoutException;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.qq.core.listeners.ConsoleListener;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;
import top.focess.scheduler.Scheduler;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * This class is used to handle input and output when executing Command.
 */
public abstract class IOHandler extends top.focess.command.IOHandler {

    protected static final Scheduler SCHEDULER = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(),"Input");

    private static final Scheduler ASYNC_SCHEDULER = Schedulers.newThreadPoolScheduler(FocessQQ.getMainPlugin(),5,true,"IOHandler");

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
        public synchronized boolean hasInput(boolean flag) {
            ConsoleListener.registerInputListener(this,SCHEDULER.run(() -> input((String) null),Duration.ofMinutes(10),"input-10-min"));
            return super.hasInput(flag);
        }

        @Override
        public synchronized boolean hasInput(boolean flag, int seconds) {
            ConsoleListener.registerInputListener(this, SCHEDULER.run(() -> input((String) null), Duration.ofSeconds(seconds),"input-" + seconds + "-sec"));
            return super.hasInput(flag);
        }
    };

    /**
     * Used to convert the input String into the target type
     * @param dataConverter the data converter
     * @return the value in the target type or null if the input is not accepted by the dataConverter
     * @param <T> the target type
     * @throws InputTimeoutException if the input timeout
     */
    @Nullable
    public <T> T input(DataConverter<T> dataConverter) throws InputTimeoutException {
        String input = input();
        if (dataConverter.accept(input))
            return dataConverter.convert(input);
        return null;
    }

    public static IOHandler getConsoleIoHandler() {
        return CONSOLE_IO_HANDLER;
    }

    public static void setConsoleIoHandler(final IOHandler consoleIoHandler) {
        CONSOLE_IO_HANDLER = consoleIoHandler;
    }

    public void async(Consumer<IOHandler> consumer) {
        ASYNC_SCHEDULER.run(() -> consumer.accept(this), "async");
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

    /**
     * Indicate there needs an input String and wait for the seconds
     *
     * @param seconds the timeout seconds
     * @param flag true if you need the MiraiCode of this input when it is a Mirai Message, false if you need the string value of this input
     * @return true if there is an input String, false otherwise
     * @see #hasInput(boolean)
     */
    public abstract boolean hasInput(boolean flag, int seconds);
}
