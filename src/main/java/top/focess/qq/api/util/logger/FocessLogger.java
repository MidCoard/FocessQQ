package top.focess.qq.api.util.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.focess.qq.FocessQQ;
import top.focess.qq.core.commands.util.ChatConstants;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

/**
 * This is a logger util class.
 */
public class FocessLogger {

    private static final Logger LOG = LoggerFactory.getLogger(FocessLogger.class);

    private boolean debugOutput;

    /**
     * Log a message with INFO level
     *
     * @param message the message need to info
     */
    public void info(final String message) {
        LOG.info(ChatConstants.CONSOLE_OUTPUT_HEADER + message);
    }

    /**
     * Log a message with INFO level
     *
     * @param key     the language key
     * @param objects the objects need to replace
     */
    public void infoLang(final String key, final Object... objects) {
        this.info(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Log a message inputted by console with DEBUG level
     *
     * @param message the message need to reshow
     */
    public void consoleInput(final String message) {
        LOG.debug(ChatConstants.CONSOLE_INPUT_HEADER + message);
    }

    /**
     * Log a message and a throwable (or exception) with ERROR level
     *
     * @param message the message need to error
     * @param e       a throwable (or exception) with this message
     */
    public void thr(final String message, final Throwable e) {
        LOG.error(message, e);
        if (this.debugOutput && FocessQQ.getAdministrator() != null)
            FocessQQ.getAdministrator().sendMessage(message + ", " + e.getMessage());
    }

    /**
     * Log a message and a throwable (or exception) with ERROR level
     *
     * @param key     the language key
     * @param e       a throwable (or exception) with this message
     * @param objects the objects need to replace
     */
    public void thrLang(final String key, final Throwable e, final Object... objects) {
        this.thr(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects), e);
    }


    /**
     * Log a message with ERROR level
     *
     * @param message the message need to fatal
     */
    public void fatal(final String message) {
        LOG.error(ChatConstants.CONSOLE_FATAL_HEADER + message);
    }


    /**
     * Log a message with ERROR level
     *
     * @param key     the language key
     * @param objects the objects need to replace
     */
    public void fatalLang(final String key, final Object... objects) {
        this.fatal(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Log a message with DEBUG level
     *
     * @param message the message need to debug
     */
    public void debug(final String message) {
        if (this.debugOutput)
            this.info(ChatConstants.DEBUG_HEADER + message);
        else
            LOG.debug(ChatConstants.CONSOLE_DEBUG_HEADER + message);
    }

    /**
     * Log a message with DEBUG level
     *
     * @param key     the language key
     * @param objects the objects need to replace
     */
    public void debugLang(final String key, final Object... objects) {
        this.debug(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Log a message and a throwable (or exception) with TRACE level
     *
     * @param message the message need to trace
     * @param e       a throwable (or exception) with this message
     */
    public void trace(final String message, final Throwable e) {
        LOG.trace(message, e);
    }

    /**
     * Toggle debug output
     *
     * Note: if debug output is true, the debug message will be outputted as INFO level and DEBUG level, otherwise, it will be outputted as DEBUG level
     */
    public void toggleDebugOutput() {
        this.debugOutput = !this.debugOutput;
    }

    /**
     * Get debug output status
     * @return true if debug output as INFO level and DEBUG level, false otherwise
     */
    public boolean isDebugOutput() {
        return this.debugOutput;
    }
}
