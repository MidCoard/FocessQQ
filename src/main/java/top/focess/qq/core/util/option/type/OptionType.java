package top.focess.qq.core.util.option.type;


public abstract class OptionType<T> {

    public static final OptionType<String> DEFAULT_OPTION_TYPE = new OptionType<String>() {
        @Override
        public String parse(final String v) {
            return v;
        }

        @Override
        public boolean accept(final String v) {
            return true;
        }

        @Override
        public String toString() {
            return "DEFAULT_OPTION_TYPE";
        }
    };

    public abstract T parse(String v);

    public abstract boolean accept(String v);
}
