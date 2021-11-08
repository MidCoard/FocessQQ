package com.focess.api.util.logger;

import com.focess.Main;
import com.focess.core.commands.util.ChatConstants;
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
     * Log a message with ERROR level
     *
     * @param message the message need to fatal
     */
    public void fatal(String message) {LOG.error(ChatConstants.CONSOLE_FATAL_HEADER + message);}


    /**
     * Log a message with DEBUG level
     *
     * @param message the message need to debug
     */
    public void debug(String message) {
        LOG.debug(ChatConstants.CONSOLE_DEBUG_HEADER + message);
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
