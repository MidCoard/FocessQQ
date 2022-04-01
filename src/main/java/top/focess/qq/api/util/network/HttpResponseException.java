package top.focess.qq.api.util.network;

import java.io.IOException;

/**
 * Thrown to indicate that the request was not successful
 */
public class HttpResponseException extends IOException {

    /**
     * Constructs a HttpResponseException
     * @param exception the cause
     */
    public HttpResponseException(Exception exception) {
        super("This request was not successful.", exception);
    }
}
