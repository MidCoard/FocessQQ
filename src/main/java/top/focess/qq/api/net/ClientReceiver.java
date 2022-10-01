package top.focess.qq.api.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.net.PackHandler;
import top.focess.net.packet.Packet;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

import java.util.List;
import java.util.Map;

@PermissionEnv(values = {Permission.SEND_PACKET, Permission.RECEIVE_PACKET})
public class ClientReceiver {

    private final Map<Plugin, Map<Class<?>, List<PackHandler>>> handlers = Maps.newConcurrentMap();

    private final top.focess.net.receiver.ClientReceiver receiver;

    public ClientReceiver(top.focess.net.receiver.ClientReceiver receiver) {
        this.receiver = receiver;
    }

    public void close() {
        receiver.close();
        this.handlers.clear();
    }

    public void unregisterAll() {
        receiver.unregisterAll();
        this.handlers.clear();
    }

    public void unregister(Plugin plugin) {
        Map<Class<?>, List<PackHandler>> handlers = this.handlers.getOrDefault(plugin, Maps.newConcurrentMap());
        handlers.values().forEach(v->v.forEach(receiver::unregister));
        this.handlers.remove(plugin);
    }

    public void sendPacket(Packet packet) {
        Permission.checkPermission(Permission.SEND_PACKET);
        receiver.sendPacket(packet);
    }

    public <T extends Packet> void register(Plugin plugin, Class<T> packet, PackHandler<T> packHandler) {
        Permission.checkPermission(Permission.RECEIVE_PACKET);
        handlers.compute(plugin, (k,v)->{
            if (v == null)
                v = Maps.newConcurrentMap();
            v.compute(packet, (k1,v1)->{
                if (v1 == null)
                    v1 = Lists.newArrayList();
                v1.add(packHandler);
                return v1;
            });
            return v;
        });
        receiver.register(packet,packHandler);
    }

    public String getName() {
        return receiver.getName();
    }

    public String getHost() {
        return receiver.getHost();
    }

    public int getPort() {
        return receiver.getPort();
    }

    public boolean isConnected() {
        return receiver.isConnected();
    }

    public int getClientId() {
        return receiver.getClientId();
    }

    public String getClientToken() {
        return receiver.getClientToken();
    }
}
