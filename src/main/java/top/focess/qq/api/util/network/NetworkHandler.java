package top.focess.qq.api.util.network;

import okhttp3.MediaType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.commands.util.ChatConstants;
import top.focess.util.network.HttpHandler;
import top.focess.util.network.HttpResponse;

import java.util.Map;

/**
 * This is a network util class.
 */
public class NetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);

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
    public NetworkHandler(Plugin plugin, top.focess.util.network.NetworkHandler.Options options) {
        this.plugin = plugin;
        this.networkHandler = new top.focess.util.network.NetworkHandler(options);
        this.networkHandler.addHandler(new HttpHandler() {
            @Override
            public void handle(String url, Map<String, Object> data, Map<String, String> header, String body) {
                LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + plugin.getName() + "] " + url + " Get: " + data + " with Header: " + header + ", Response: " + body);
            }

            @Override
            public void handleException(String url, Map<String, Object> data, Map<String, String> header, Exception e) {
                LOGGER.debug(ChatConstants.NETWORK_DEBUG_HEADER + "[" + plugin.getName() + "] " + url + " Post: " + data + " with Header: " + header + ", Error: " + e.getMessage());
            }
        });
    }

    /**
     * Initialize a new network handler with default options
     * @param plugin the plugin
     */
    public NetworkHandler(Plugin plugin) {
        this(plugin, top.focess.util.network.NetworkHandler.Options.ofNull());
    }

    public Plugin getPlugin() {
        return plugin;
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

    public HttpResponse put(String url, Map<String, Object> data, Map<String, String> header, @NotNull MediaType mediaType) {
        return networkHandler.put(url, data, header, mediaType);
    }

    public HttpResponse post(String url, Map<String, Object> data, Map<String, String> header, @NotNull MediaType mediaType) {
        return networkHandler.post(url, data, header, mediaType);
    }

    public HttpResponse get(String url, @NotNull Map<String, Object> data, Map<String, String> header) {
        return networkHandler.get(url, data, header);
    }
}
