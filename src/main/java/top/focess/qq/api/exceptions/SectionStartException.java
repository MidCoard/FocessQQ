package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate that the section is already started
 */
public class SectionStartException extends RuntimeException {

    /**
     * Constructs a SectionStartException
     * @param name the section name
     */
    public SectionStartException(String name) {
        super("Section " + name + " is already started.");
    }
}
