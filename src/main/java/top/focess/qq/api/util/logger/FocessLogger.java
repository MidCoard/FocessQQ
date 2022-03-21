package top.focess.qq.api.util.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.focess.qq.core.commands.util.ChatConstants;
import top.focess.qq.core.plugin.PluginCoreClassLoader;
import top.focess.qq.core.util.MethodCaller;

/**
 * This is a logger util class.
 */
public class FocessLogger {

    private static final Logger LOG = LoggerFactory.getLogger(FocessLogger.class);

    /**
     * Log a message with INFO level
     *
     * @param message the message need to info
     */
    public void info(String message) {
        LOG.info(ChatConstants.CONSOLE_OUTPUT_HEADER + message);
    }

    /**
     * Log a message with INFO level
     *
     * @param key the language key
     * @param objects the objects need to replace
     */
    public void infoLang(String key,Object... objects) {
        info(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Log a message inputted by console with DEBUG level
     *
     * @param message the message need to reshow
     */
    public void consoleInput(String message) {
        LOG.debug(ChatConstants.CONSOLE_INPUT_HEADER + message);
    }

    /**
     * Log a message and a throwable (or exception) with ERROR level
     *
     * @param message the message need to error
     * @param e a throwable (or exception) with this message
     */
    public void thr(String message,Throwable e) {
        LOG.error(message,e);
    }

    /**
     * Log a message and a throwable (or exception) with ERROR level
     * @param key the language key
     * @param e a throwable (or exception) with this message
     * @param objects the objects need to replace
     */
    public void thrLang(String key,Throwable e,Object... objects) {
        thr(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects),e);
    }


    /**
     * Log a message with ERROR level
     *
     * @param message the message need to fatal
     */
    public void fatal(String message) {LOG.error(ChatConstants.CONSOLE_FATAL_HEADER + message);}


    /**
     * Log a message with ERROR level
     *
     * @param key the language key
     * @param objects the objects need to replace
     */
    public void fatalLang(String key,Object... objects) {
        fatal(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Log a message with DEBUG level
     *
     * @param message the message need to debug
     */
    public void debug(String message) {
        LOG.debug(ChatConstants.CONSOLE_DEBUG_HEADER + message);
    }

    /**
     * Log a message with DEBUG level
     *
     * @param key the language key
     * @param objects the objects need to replace
     */
    public void debugLang(String key,Object... objects) {
        debug(String.format(PluginCoreClassLoader.getClassLoadedByOrDefault(MethodCaller.getCallerClass()).getLangConfig().get(key), objects));
    }

    /**
     * Log a message and a throwable (or exception) with TRACE level
     *
     * @param message the message need to trace
     * @param e a throwable (or exception) with this message
     */
    public void trace(String message,Throwable e) {
        LOG.trace(message,e);
    }
}
