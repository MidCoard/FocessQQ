package top.focess.qq.api.util.network;

import com.google.common.collect.Maps;
import okhttp3.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.util.json.JSON;
import top.focess.qq.core.commands.util.ChatConstants;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This is a network util class.
 */
public class NetworkHandler {

    /**
     * Used to indicate this http-request accepts JSON
     */
    @NonNull
    public static final MediaType JSON = Objects.requireNonNull(MediaType.parse("application/json; charset=utf-8"));
    /**
     * Used to indicate this http-request accepts normal String
     */
    @NonNull
    public static final MediaType TEXT = Objects.requireNonNull(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"));
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);
    private static final OkHttpClient CLIENT;

    static {
        final X509TrustManager[] managers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) {

                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, managers, new SecureRandom());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        CLIENT = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).sslSocketFactory(sslContext.getSocketFactory(), managers[0]).hostnameVerifier((hostname, session) -> true).build();
    }

    private final Plugin plugin;

    public NetworkHandler(final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Send a http-request
     *
     * @param url         the request url
     * @param data        the request data
     * @param requestType the request type
     * @return the response of this request
     */
    public HttpResponse request(final String url, final Map<String, Object> data, final RequestType requestType) {
        return this.request(url, data, Maps.newHashMap(), TEXT, requestType);
    }

    /**
     * Send a http-request
     *
     * @param url         the request url
     * @param requestType the request type
     * @return the response of this request
     * @see NetworkHandler#request(String, Map, RequestType)
     */
    public HttpResponse request(final String url, final RequestType requestType) {
        return this.request(url, Maps.newHashMap(), requestType);
    }

    /**
     * Send a http-request
     *
     * @param url         the request url
     * @param data        the request data
     * @param header      the request header
     * @param mediaType   the request acceptable type
     * @param requestType the request type
     * @return the response of this request
     */
    public HttpResponse request(final String url, final Map<String, Object> data, final Map<String, String> header, final MediaType mediaType, final RequestType requestType) {
        if (requestType == RequestType.GET)
            return this.get(url, data, header);
        else if (requestType == RequestType.POST)
            return this.post(url, data, header, mediaType);
        else if (requestType == RequestType.PUT)
            return this.put(url, data, header, mediaType);
        return HttpResponse.ofNull(this.plugin);
    }

    private String process(@NotNull final Map<String, Object> data) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final String key : data.keySet())
            stringBuilder.append(key).append('=').append(data.get(key)).append('&');
        if (stringBuilder.length() != 0)
            return stringBuilder.substring(0, stringBuilder.length() - 1);
        return "";
    }

    /**
     * Send a PUT http-request
     *
     * @param url       the request url
     * @param data      the request data
     * @param header    the request header
     * @param mediaType the request acceptable type
     * @return the response of this request
     */
    public HttpResponse put(final String url, final Map<String, Object> data, final Map<String, String> header, @NotNull final MediaType mediaType) {
        final String value;
        if (mediaType.equals(JSON))
            value = new JSON(data).toJson();
        else value = this.process(data);
        final RequestBody requestBody = RequestBody.create(value, mediaType);
        final Request request = new Request.Builder().url(url).headers(Headers.of(header)).put(requestBody).build();
        try {
            final Response response = CLIENT.newCall(request).execute();
            // Call#execute() returns a non-null Response object
            final String body = Objects.requireNonNull(response.body()).string();
            LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + this.plugin.getName() + "] " + url + " Put: " + data + " with Header: " + header + ", Response: " + body);
            return new HttpResponse(this.plugin, response.code(), response.headers(), body);
        } catch (final Exception e) {
            LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + this.plugin.getName() + "] " + url + " Put: " + data + " with Header: " + header + ", Error: " + e.getMessage());
            return new HttpResponse(this.plugin, e);
        }
    }

    /**
     * Send a POST http-request
     *
     * @param url       the request url
     * @param data      the request data
     * @param header    the request header
     * @param mediaType the request acceptable type
     * @return the response of this request
     */
    public HttpResponse post(final String url, final Map<String, Object> data, final Map<String, String> header, @NotNull final MediaType mediaType) {
        final String value;
        if (mediaType.equals(JSON))
            value = new JSON(data).toJson();
        else value = this.process(data);
        final RequestBody requestBody = RequestBody.create(value, mediaType);
        final Request request = new Request.Builder().url(url).headers(Headers.of(header)).post(requestBody).build();
        try {
            final Response response = CLIENT.newCall(request).execute();
            // Call#execute() returns a non-null Response object
            final String body = Objects.requireNonNull(response.body()).string();
            LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + this.plugin.getName() + "] " + url + " Post: " + data + " with Header: " + header + ", Response: " + body);
            return new HttpResponse(this.plugin, response.code(), response.headers(), body);
        } catch (final IOException e) {
            LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + this.plugin.getName() + "] " + url + " Post: " + data + " with Header: " + header + ", Error: " + e.getMessage());
            return new HttpResponse(this.plugin, e);
        }
    }

    /**
     * Send a GET http-request
     *
     * @param url    the request url
     * @param data   the request data
     * @param header the request header
     * @return the response of this request
     */
    public HttpResponse get(final String url, @NotNull final Map<String, Object> data, final Map<String, String> header) {
        final Request request;
        if (data.size() != 0)
            request = new Request.Builder().url(url + "?" + this.process(data)).get().headers(Headers.of(header)).build();
        else
            request = new Request.Builder().url(url).get().headers(Headers.of(header)).build();
        try {
            final Response response = CLIENT.newCall(request).execute();
            // Call#execute() returns a non-null Response object
            final String body = Objects.requireNonNull(response.body()).string();
            LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + this.plugin.getName() + "] " + url + " Get: " + data + " with Header: " + header + ", Response: " + body);
            return new HttpResponse(this.plugin, response.code(), response.headers(), body);
        } catch (final IOException e) {
            LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + this.plugin.getName() + "] " + url + " Get: " + data + " with Header: " + header + ", Error: " + e.getMessage());
            return new HttpResponse(this.plugin, e);
        }
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public enum RequestType {
        /**
         * HTTP GET Request Method
         */
        GET,
        /**
         * HTTP POST Request Method
         */
        POST,
        /**
         * HTTP PUT Request Method
         */
        PUT
    }

}
