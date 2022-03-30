package top.focess.qq.core.util.option;

public class UnsupportedOptionException extends UnsupportedOperationException {
    public UnsupportedOptionException(final String option) {
        super("The option " + option + " is not supported.");
    }
}
