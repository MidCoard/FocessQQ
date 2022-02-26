package com.focess.api.util.logger;

import com.focess.Main;
import com.focess.api.plugin.Plugin;
import com.focess.core.commands.util.ChatConstants;
import com.focess.core.plugin.PluginCoreClassLoader;
import com.focess.core.util.MethodCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a logger util class.
 */
public class FocessLogger {

    private static final Logger LOG = LoggerFactory.getLogger(FocessLogger.class);

    private static FocessLogger FOCESS_LOG;

    /**
     * Initialize a logger for this framework.
     * Never instance it! It will be instanced when bot bootstraps automatically.
     *
     * @see Main#getLogger()
     */
    public FocessLogger() {
        if (FOCESS_LOG != null)
            throw new UnsupportedOperationException();
        FOCESS_LOG = this;
    }

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
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin == null)
            info(String.format(Main.getLangConfig().get(key), objects));
        else info(String.format(plugin.getLangConfig().get(key), objects));
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
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin == null)
            thr(String.format(Main.getLangConfig().get(key), objects),e);
        else thr(String.format(plugin.getLangConfig().get(key), objects),e);
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
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin == null)
            fatal(String.format(Main.getLangConfig().get(key), objects));
        else fatal(String.format(plugin.getLangConfig().get(key), objects));
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
        Plugin plugin = PluginCoreClassLoader.getClassLoadedBy(MethodCaller.getCallerClass());
        if (plugin == null)
            debug(String.format(Main.getLangConfig().get(key), objects));
        else debug(String.format(plugin.getLangConfig().get(key), objects));
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
