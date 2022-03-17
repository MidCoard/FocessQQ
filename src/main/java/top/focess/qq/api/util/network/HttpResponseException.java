package top.focess.qq.api.util.network;

import java.io.IOException;

/**
 * Thrown to indicate that the request was not successful
 */
public class HttpResponseException extends IOException {

    /**
     * Constructs a HttpResponseException
     */
    public HttpResponseException() {
        super("This request was not successful.");
    }
}
