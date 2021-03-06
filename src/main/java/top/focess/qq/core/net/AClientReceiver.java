package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.ClientReceiver;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.packet.Packet;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;
import java.util.Map;

public abstract class AClientReceiver implements ClientReceiver {

    protected final String host;
    protected final int port;
    protected final String name;
    protected final Map<Plugin, Map<Class<?>, List<PackHandler>>> packHandlers = Maps.newConcurrentMap();
    protected String token;
    protected int id;
    protected volatile boolean connected;

    public AClientReceiver(final String host, final int port, final String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    @Override
    public <T extends Packet> void register(final Plugin plugin, final Class<T> c, final PackHandler<T> packHandler) {
        this.packHandlers.compute(plugin, (k, v) -> {
            if (v == null)
                v = Maps.newHashMap();
            v.compute(c, (k1, v1) -> {
                if (v1 == null)
                    v1 = Lists.newArrayList();
                v1.add(packHandler);
                return v1;
            });
            return v;
        });
    }

    @Override
    public void unregister(final Plugin plugin) {
        this.packHandlers.remove(plugin);
    }

    @Override
    public boolean unregisterAll() {
        boolean ret = false;
        for (final Plugin plugin : this.packHandlers.keySet()) {
            if (plugin != FocessQQ.getMainPlugin())
                ret = true;
            this.unregister(plugin);
        }
        return ret;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public int getClientId() {
        return this.id;
    }

    @Override
    public String getClientToken() {
        return this.token;
    }

    public String getName() {
        return this.name;
    }

}
