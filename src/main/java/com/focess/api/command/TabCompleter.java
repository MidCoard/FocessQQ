package com.focess.api.command;


import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class TabCompleter<T> extends DataConverter<T> {


    public static final Predicate<String> integerPredicate = i -> {
        try {
            Integer.parseInt(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    public static final Predicate<String> UUIDPredicate = i -> {
        try {
            UUID.fromString(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    public static final Predicate<String> doublePredicate = i -> {
        try {
            Double.parseDouble(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    public static final Predicate<String> longPredicate = i -> {
        try {
            Long.parseLong(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    abstract List<String> getTabComplete(CommandSender sender);

}