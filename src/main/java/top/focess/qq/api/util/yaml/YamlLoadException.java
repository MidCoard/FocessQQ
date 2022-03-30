package top.focess.qq.api.util.yaml;

import java.io.IOException;

/**
 * Thrown to indicate there is any exception thrown in the yaml loading process
 */
public class YamlLoadException extends IOException {

    /**
     * Constructs a YamlLoadException
     * @param e the exception
     */
    public YamlLoadException(final Exception e) {
        super(e);
    }
}
