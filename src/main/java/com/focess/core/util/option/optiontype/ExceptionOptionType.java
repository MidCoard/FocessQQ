package com.focess.core.util.option.optiontype;


public abstract class ExceptionOptionType<T> extends OptionType<T> {

    @Override
    public boolean accept(String v) {
        try {
            this.parse(v);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
