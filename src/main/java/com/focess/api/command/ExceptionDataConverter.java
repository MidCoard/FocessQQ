package com.focess.api.command;

public abstract class ExceptionDataConverter<T> extends DataConverter<T>{

    @Override
    protected boolean accept(String arg) {
        try {
            convert(arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
