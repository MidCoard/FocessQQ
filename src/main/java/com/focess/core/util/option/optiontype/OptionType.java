package com.focess.core.util.option.optiontype;

public abstract class OptionType<T> {

    public abstract T parse(String v);

    public abstract boolean accept(String v);

    public static final OptionType<String> DEFAULT_OPTION_TYPE = new OptionType<String>() {
        @Override
        public String parse(String v) {
            return v;
        }

        @Override
        public boolean accept(String v) {
            return true;
        }

        @Override
        public String toString() {
            return "DEFAULT_OPTION_TYPE";
        }
    };
}
