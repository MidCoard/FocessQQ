package top.focess.qq.api.command;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * This class used to convert String data to target T type data.
 *
 * @param <T> target type
 */
public abstract class DataConverter<T> {

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

    /**
     * Never convert it! Put them into DataCollection with their original values.
     */
    public static final DataConverter<String> DEFAULT_DATA_CONVERTER = new DataConverter<String>() {
        @Override
        protected boolean accept(final String arg) {
            return true;
        }

        @Override
        public String convert(final String arg) {
            return arg;
        }

        @Override
        protected Class<String> getTargetClass() {
            return String.class;
        }
    };


    /**
     * Convert the String argument to Integer argument
     */
    public static final DataConverter<Integer> INTEGER_DATA_CONVERTER = new DataConverter<Integer>() {
        @Override
        protected boolean accept(final String arg) {
            return INTEGER_PREDICATE.test(arg);
        }

        @NotNull
        @Override
        public Integer convert(final String arg) {
            return Integer.parseInt(arg);
        }

        @Override
        protected Class<Integer> getTargetClass() {
            return Integer.class;
        }
    };

    /**
     * Convert the String argument to Long argument
     */
    public static final DataConverter<Long> LONG_DATA_CONVERTER = new DataConverter<Long>() {
        @Override
        protected boolean accept(final String arg) {
            return LONG_PREDICATE.test(arg);
        }

        @NotNull
        @Override
        public Long convert(final String arg) {
            return Long.parseLong(arg);
        }

        @Override
        protected Class<Long> getTargetClass() {
            return Long.class;
        }
    };

    /**
     * Convert the String argument to Double argument
     */
    public static final DataConverter<Double> DOUBLE_DATA_CONVERTER = new DataConverter<Double>() {
        @Override
        protected boolean accept(final String s) {
            return DOUBLE_PREDICATE.test(s);
        }

        @NotNull
        @Override
        public Double convert(final String s) {
            return Double.parseDouble(s);
        }

        @Override
        protected Class<Double> getTargetClass() {
            return Double.class;
        }
    };

    /**
     * Convert the String argument to Boolean argument
     */
    public static final DataConverter<Boolean> BOOLEAN_DATA_CONVERTER = new DataConverter<Boolean>() {
        @Override
        protected boolean accept(final String arg) {
            return arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false");
        }

        @Override
        public Boolean convert(final String arg) {
            return Boolean.parseBoolean(arg);
        }

        @Override
        protected Class<Boolean> getTargetClass() {
            return Boolean.class;
        }
    };

    /**
     * Indicate whether this String argument is this target type or not
     *
     * @param arg the target argument in String
     * @return true if this String argument can convert to this target type, false otherwise
     */
    protected abstract boolean accept(String arg);

    /**
     * Convert String argument to target argument
     *
     * @param arg the target argument in String
     * @return the target argument
     */
    public abstract T convert(String arg);

    boolean put(final DataCollection dataCollection, final String arg) {
        if (this.accept(arg)) {
            this.connect(dataCollection, this.convert(arg));
            return true;
        }
        return false;
    }

    void connect(@NotNull final DataCollection dataCollection, final T arg) {
        dataCollection.write(this.getTargetClass(), arg);
    }

    protected abstract Class<T> getTargetClass();
}
