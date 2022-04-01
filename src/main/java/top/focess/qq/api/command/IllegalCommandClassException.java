package top.focess.qq.api.command;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown to indicate this class is an illegal Command class
 */
public class IllegalCommandClassException extends IllegalArgumentException {
    /**
     * Constructs a IllegalCommandClassException
     *
     * @param c the illegal command class
     */
    public IllegalCommandClassException(@NotNull final Class<?> c) {
        super("The class " + c.getName() + " is an illegal Command class.");
    }
}
