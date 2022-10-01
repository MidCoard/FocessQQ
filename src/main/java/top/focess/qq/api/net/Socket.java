package top.focess.qq.api.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.net.receiver.Receiver;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class Socket {

    private final top.focess.net.socket.Socket socket;

    private final Map<Plugin, List<Receiver>> receivers = Maps.newConcurrentMap();

    public Socket(top.focess.net.socket.Socket socket) {
        this.socket = socket;
    }

    public void registerReceiver(Plugin plugin, Receiver receiver) {
        receivers.compute(plugin, (k,v)->{
            if (v == null)
                v = Lists.newCopyOnWriteArrayList();
            v.add(receiver);
            return v;
        });
        this.socket.registerReceiver(receiver);
    }

    public boolean containsServerSide() {
        return this.socket.containsServerSide();
    }

    public boolean containsClientSide() {
        return this.socket.containsClientSide();
    }

    public void close() {
        this.socket.close();
        this.receivers.clear();
    }

    public void unregister(Plugin plugin) {
        List<Receiver> receivers = this.receivers.getOrDefault(plugin,Lists.newArrayList());
        receivers.forEach(this.socket::unregister);
        this.receivers.remove(plugin);
    }

    public void unregisterAll() {
        this.socket.unregisterAll();
        this.receivers.clear();
    }
}
