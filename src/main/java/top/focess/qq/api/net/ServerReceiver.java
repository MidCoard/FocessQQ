package top.focess.qq.api.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.net.Client;
import top.focess.net.PackHandler;
import top.focess.net.packet.Packet;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

import java.util.List;
import java.util.Map;

@PermissionEnv(values = {Permission.SEND_PACKET, Permission.RECEIVE_PACKET})
public class ServerReceiver {

    protected final top.focess.net.receiver.ServerReceiver receiver;

    private final Map<Plugin, Map<String, Map<Class<?>, List<PackHandler>>>> handlers = Maps.newConcurrentMap();

    public ServerReceiver(top.focess.net.receiver.ServerReceiver receiver) {
        this.receiver = receiver;
    }

    public void sendPacket(String s, Packet packet) {
        Permission.checkPermission(Permission.SEND_PACKET);
        receiver.sendPacket(s,packet);
    }

    public void unregister(Plugin plugin) {
        Map<String, Map<Class<?>, List<PackHandler>>> handlers = this.handlers.getOrDefault(plugin, Maps.newConcurrentMap());
        handlers.values().forEach(v->v.values().forEach(v1->v1.forEach(receiver::unregister)));
        this.handlers.remove(plugin);
    }

    public <T extends Packet> void register(Plugin plugin, String client, Class<T> packet, PackHandler<T> packHandler) {
        Permission.checkPermission(Permission.RECEIVE_PACKET);
        handlers.compute(plugin, (k,v)->{
            if (v == null)
                v = Maps.newConcurrentMap();
            v.compute(client, (k1,v1)->{
                if (v1 == null)
                    v1 = Maps.newConcurrentMap();
                v1.compute(packet, (k2,v2)->{
                    if (v2 == null)
                        v2 = Lists.newArrayList();
                    v2.add(packHandler);
                    return v2;
                });
                return v1;
            });
            return v;
        });
        receiver.register(client,packet,packHandler);
    }

    public boolean isConnected(String client) {
        return receiver.isConnected(client);
    }

    public @Nullable Client getClient(String client) {
        return receiver.getClient(client);
    }

    public void close() {
        receiver.close();
        this.handlers.clear();
    }

    public void unregisterAll() {
        receiver.unregisterAll();
        this.handlers.clear();
    }

    public void disconnect(String client) {
        this.receiver.disconnect(client);
    }
}
