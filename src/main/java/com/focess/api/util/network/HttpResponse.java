package com.focess.api.util.network;

import com.focess.api.util.json.JSON;
import okhttp3.Headers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is used to define a response to a http-request
 */
public class HttpResponse {

    /**
     * Unknown request type error code
     */
    public static final int UNKNOWN_REQUEST = -1;

    /**
     * Exception thrown error code
     */
    public static final int EXCEPTION = -2;

    /**
     * Unknown request type HttpResponse
     */
    private static final HttpResponse UNKNOWN_REQUEST_TYPE = new HttpResponse();

    /**
     * The response code
     */
    private final int code;
    /**
     * The response headers
     */
    private Headers headers;
    /**
     * The response data
     */
    private String value;
    /**
     * The exception thrown in http-request processing
     */
    private Exception exception;

    private HttpResponse() {
        this.code = UNKNOWN_REQUEST;
    }

    /**
     * Indicate this is an exception thrown HttpResponse
     *
     * @return true if this is an exception thrown HttpResponse, false otherwise
     */
    public boolean isError() {
        return this.code == EXCEPTION;
    }

    /**
     * Initialize an exception thrown HttpResponse with e
     *
     * @param e the thrown exception in this http-request processing
     */
    public HttpResponse(Exception e) {
        this.code = EXCEPTION;
        this.exception = e;
    }

    /**
     * Initialize a HttpResponse without exceptions
     *
     * @param code the response code
     * @param headers the response header
     * @param value the response data
     */
    public HttpResponse(int code, Headers headers, String value) {
        this.code = code;
        this.value = value;
        this.headers = headers;
    }

    public static HttpResponse ofNull() {
        return UNKNOWN_REQUEST_TYPE;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    public int getCode() {
        return code;
    }

    /**
     * Get the values as JSON instance
     *
     * @return JSON instance of this response data
     */
    @NotNull
    public JSON getAsJSON() {
        return new JSON(this.value);
    }

    @Nullable
    public String getResponse() {
        return this.value;
    }

    @Nullable
    public Headers getHeaders() {
        return headers;
    }
}
