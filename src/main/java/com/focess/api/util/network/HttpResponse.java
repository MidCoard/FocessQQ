package com.focess.api.util.network;

import com.focess.api.util.json.JSON;
import okhttp3.Headers;

public class HttpResponse {

    public static final int UNKNOWN_REQUEST = -1;

    public static final int EXCEPTION = -2;


    public static HttpResponse UNKNOWN_REQUEST_TYPE = new HttpResponse();
    private final int code;
    private Headers headers;
    private String value;
    private Exception exception;

    private HttpResponse() {
        this.code = UNKNOWN_REQUEST;
    }

    public boolean isError() {
        return this.code == EXCEPTION;
    }

    public HttpResponse(Exception e) {
        this.code = EXCEPTION;
        this.exception = e;
    }

    public HttpResponse(int code, Headers headers, String value) {
        this.code = code;
        this.value = value;
        this.headers = headers;
    }

    public static HttpResponse ofNull() {
        return UNKNOWN_REQUEST_TYPE;
    }

    public Exception getException() {
        return exception;
    }

    public int getCode() {
        return code;
    }

    public JSON getAsJSON() {
        return new JSON(this.value);
    }

    public String getResponse() {
        return this.value;
    }

    public Headers getHeaders() {
        return headers;
    }
}
