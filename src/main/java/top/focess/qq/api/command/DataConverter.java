package top.focess.qq.api.command;

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
        protected boolean accept(String arg) {
            return true;
        }

        @Override
        public String convert(String arg) {
            return arg;
        }

        @Override
        protected void connect(DataCollection dataCollection, String arg) {
            dataCollection.write(arg);
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
        protected boolean accept(String arg) {
            return INTEGER_PREDICATE.test(arg);
        }

        @Override
        public Integer convert(String arg) {
            return Integer.parseInt(arg);
        }

        @Override
        protected void connect(DataCollection dataCollection, Integer arg) {
            dataCollection.writeInt(arg);
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
        protected boolean accept(String arg) {
            return LONG_PREDICATE.test(arg);
        }

        @Override
        public Long convert(String arg) {
            return Long.parseLong(arg);
        }

        @Override
        protected void connect(DataCollection dataCollection, Long arg) {
            dataCollection.writeLong(arg);
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
        protected boolean accept(String s) {
            return DOUBLE_PREDICATE.test(s);
        }

        @Override
        public Double convert(String s) {
            return Double.parseDouble(s);
        }

        @Override
        protected void connect(DataCollection dataCollection, Double arg) {
            dataCollection.writeDouble(arg);
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
        protected boolean accept(String arg) {
            return arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false");
        }

        @Override
        public Boolean convert(String arg) {
            return Boolean.parseBoolean(arg);
        }

        @Override
        protected void connect(DataCollection dataCollection, Boolean arg) {
            dataCollection.writeBoolean(arg);
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

    boolean put(DataCollection dataCollection, String arg) {
        if (this.accept(arg))
            this.connect(dataCollection, convert(arg));
        else return false;
        return true;
    }

    /**
     * Used to put data into the dataCollection
     *
     * @see DataCollection#write(Class, Object)
     * @param dataCollection where stores the data
     * @param arg the target argument
     */
    protected abstract void connect(DataCollection dataCollection, T arg);

    protected abstract Class<T> getTargetClass();
}
