package top.focess.qq.core.exceptions;

public class IllegalOptionException extends RuntimeException {
    public IllegalOptionException(String name, String arg) {
        super("This option " + name + " meets a wrong arg " + arg);
    }
}
