package top.focess.qq.api.exceptions;

/**
 * Thrown to indicate that the request was not successful
 */
public class HttpResponseException extends RuntimeException{

    /**
     * Constructs a HttpResponseException
     */
    public HttpResponseException() {
        super("This request was not successful.");
    }
}
