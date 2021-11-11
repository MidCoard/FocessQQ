package com.focess.core.util.option.optiontype;

public class LongOptionType extends ExceptionOptionType<Long>{

    public static final LongOptionType LONG_OPTION_TYPE = new LongOptionType();

    @Override
    public Long parse(String v) {
        return Long.parseLong(v);
    }
}
