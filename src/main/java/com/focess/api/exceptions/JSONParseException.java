package com.focess.api.exceptions;

public class JSONParseException extends RuntimeException{

    public JSONParseException(String json){
        super("Error in parsing JSON: " + json + ".");
    }
}
