package com.focess.core.net;

import com.focess.api.net.Client;

public class SimpleClient implements Client {

    private final String host;
    private final int port;
    private final int id;
    private final String name;
    private final String token;

    public SimpleClient(String host, int port, int id, String name, String token) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.name = name;
        this.token = token;
    }

    public SimpleClient(int id, String name, String token) {
        this.host = null;
        this.port = -1;
        this.id = id;
        this.name = name;
        this.token = token;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }
}
