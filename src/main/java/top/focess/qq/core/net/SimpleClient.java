package top.focess.qq.core.net;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.Client;

public class SimpleClient implements Client {

    private final String host;
    private final int port;
    private final int id;
    private final String name;
    private final String token;

    public SimpleClient(final String host, final int port, final int id, final String name, final String token) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.name = name;
        this.token = token;
    }

    public SimpleClient(final int id, final String name, final String token) {
        this.host = null;
        this.port = -1;
        this.id = id;
        this.name = name;
        this.token = token;
    }

    @Nullable
    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public int getId() {
        return this.id;
    }

    public String getToken() {
        return this.token;
    }

    public String getName() {
        return this.name;
    }
}
