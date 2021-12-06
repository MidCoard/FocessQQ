package com.focess.core.util.option.type;

public class LongOptionType extends ExceptionOptionType<Long>{

    public static final LongOptionType LONG_OPTION_TYPE = new LongOptionType();

    @Override
    public Long parse(String v) {
        return Long.parseLong(v);
    }

    @Override
    public String toString() {
        return "LONG_OPTION_TYPE";
    }
}
