package top.focess.qq.core.util.option;

public class IllegalOptionException extends IllegalStateException {
    public IllegalOptionException(final String name, final String arg) {
        super("This option " + name + " meets a wrong arg " + arg);
    }
}
