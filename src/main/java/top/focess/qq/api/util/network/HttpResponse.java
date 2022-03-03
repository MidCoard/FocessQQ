package top.focess.qq.api.util.network;

import okhttp3.Headers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.exceptions.HttpResponseException;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.json.JSON;

import java.util.function.Function;

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
    private static final Function<Plugin,HttpResponse> UNKNOWN_REQUEST_TYPE = HttpResponse::new;

    /**
     * The response code
     */
    private final int code;

    /**
     * The request plugin
     */
    private final Plugin plugin;
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

    /**
     * Initialize a HttpResponse with code
     *
     * @param plugin the request plugin
     * @param code the response code
     */
    private HttpResponse(Plugin plugin,int code) {
        this.code = code;
        this.plugin = plugin;
    }

    private HttpResponse(Plugin plugin) {
        this(plugin,UNKNOWN_REQUEST);
    }

    /**
     * Initialize an exception thrown HttpResponse with e
     *
     * @param plugin the request plugin
     * @param e the thrown exception in this http-request processing
     */
    public HttpResponse(Plugin plugin,Exception e) {
        this(plugin,EXCEPTION);
        this.exception = e;
    }

    /**
     * Initialize a HttpResponse without exceptions
     *
     * @param code the response code
     * @param headers the response header
     * @param value the response data
     */
    public HttpResponse(Plugin plugin,int code, Headers headers, String value) {
        this(plugin,code);
        this.value = value;
        this.headers = headers;
    }

    public static HttpResponse ofNull(Plugin plugin) {
        return UNKNOWN_REQUEST_TYPE.apply(plugin);
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
        if (isError())
            throw new HttpResponseException();
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

    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Indicate this is an exception thrown HttpResponse
     *
     * @return true if this is an exception thrown HttpResponse, false otherwise
     */
    public boolean isError() {
        return this.code == EXCEPTION;
    }

}
