package com.focess.core.util.option.optiontype;

public class IntegerOptionType extends ExceptionOptionType<Integer>{

    public static final IntegerOptionType INTEGER_OPTION_TYPE = new IntegerOptionType();

    @Override
    public Integer parse(String v) {
        return Integer.parseInt(v);
    }

}
