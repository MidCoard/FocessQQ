package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate there is any exception thrown in the yaml loading process
 */
public class YamlLoadException extends RuntimeException {

    /**
     * Constructs a YamlLoadException
     * @param e the exception
     */
    public YamlLoadException(Exception e) {
        super(e);
    }
}
