package com.focess.api.command;


import java.util.List;
import java.util.function.Predicate;

/**
 * This class used to define how to complete the rest arguments of the command with pressing TAB.
 * This class now is {@link Deprecated}, because the pressing TAB now cannot be realized.
 *
 * @param <T> target type
 */
@Deprecated
public abstract class TabCompleter<T> extends DataConverter<T> {


    /**
     * It is a Predicate used to predicate a String is an Integer
     */
    public static final Predicate<String> INTEGER_PREDICATE = i -> {
        try {
            Integer.parseInt(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    /**
     * It is a Predicate used to predicate a String is a Double
     */
    public static final Predicate<String> DOUBLE_PREDICATE = i -> {
        try {
            Double.parseDouble(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    /**
     * It is a Predicate used to predicate a String is a Long
     */
    public static final Predicate<String> LONG_PREDICATE = i -> {
        try {
            Long.parseLong(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    abstract List<String> getTabComplete(CommandSender sender);

}