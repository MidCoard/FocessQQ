package top.focess.qq.api.util.network;

import okhttp3.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.commands.util.ChatConstants;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;
import top.focess.util.network.HttpHandler;
import top.focess.util.network.HttpResponse;

import java.util.Map;
import java.util.Objects;

/**
 * This is a network util class.
 */
@PermissionEnv(values = Permission.NETWORK)
public class NetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);
    @NonNull
    public static final MediaType JSON = Objects.requireNonNull(MediaType.parse("application/json; charset=utf-8"));
    @NonNull
    public static final MediaType TEXT = Objects.requireNonNull(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"));

    /**
     * The plugin
     */
    private final Plugin plugin;
    private final top.focess.util.network.NetworkHandler networkHandler;

    /**
     * Initialize a new network handler with specified options
     * @param plugin the plugin
     * @param options the options
     */
    public NetworkHandler(final Plugin plugin, final top.focess.util.network.NetworkHandler.Options options) {
        Permission.checkPermission(Permission.NETWORK);
        this.plugin = plugin;
        this.networkHandler = new top.focess.util.network.NetworkHandler(options);
        this.networkHandler.addHandler(new HttpHandler() {
            @Override
            public void handle(final String url, final String data, final Map<String, String> header, final String body) {
                LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + plugin.getName() + "] " + url + " Request: " + data + " with Header: " + header + ", Response: " + body);
            }

            @Override
            public void handleException(final String url, final String data, final Map<String, String> header, final Exception e) {
                LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + plugin.getName() + "] " + url + " Request: " + data + " with Header: " + header + ", Error: " + e.getMessage());
            }
        });
    }

    /**
     * Initialize a new network handler with default options
     * @param plugin the plugin
     */
    public NetworkHandler(final Plugin plugin) {
        this(plugin, top.focess.util.network.NetworkHandler.Options.ofNull());
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public HttpResponse request(String url, Map<String, Object> data, top.focess.util.network.NetworkHandler.RequestType requestType) {
        return networkHandler.request(url, data, requestType);
    }

    public HttpResponse request(String url, top.focess.util.network.NetworkHandler.RequestType requestType) {
        return networkHandler.request(url, requestType);
    }

    public HttpResponse request(String url, Map<String, Object> data, Map<String, String> header, MediaType mediaType, top.focess.util.network.NetworkHandler.RequestType requestType) {
        return networkHandler.request(url, data, header, mediaType, requestType);
    }

    public HttpResponse request(String url, String data, Map<String, String> header, MediaType mediaType, top.focess.util.network.NetworkHandler.RequestType requestType) {
        return networkHandler.request(url, data, header, mediaType, requestType);
    }

    public HttpResponse put(String url, String data, Map<String, String> header, @NotNull MediaType mediaType) {
        return networkHandler.put(url, data, header, mediaType);
    }

    public HttpResponse post(String url, String data, Map<String, String> header, @NotNull MediaType mediaType) {
        return networkHandler.post(url, data, header, mediaType);
    }

    public HttpResponse delete(String url, String data, Map<String, String> header, @NotNull MediaType mediaType) {
        return networkHandler.delete(url, data, header, mediaType);
    }

    public HttpResponse get(String url, @NotNull Map<String, Object> data, Map<String, String> header) {
        return networkHandler.get(url, data, header);
    }
}
