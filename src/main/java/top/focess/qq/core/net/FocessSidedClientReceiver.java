package top.focess.qq.core.net;

import top.focess.qq.Main;
import top.focess.qq.api.net.ClientReceiver;
import top.focess.qq.api.net.PackHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import top.focess.qq.api.net.packet.*;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class FocessSidedClientReceiver implements ClientReceiver {

    private final String host;
    private final int port;
    private final String name;
    private final FocessSidedClientSocket focessSidedClientSocket;
    private String token;
    private int id;
    private volatile boolean connected = false;
    private final Map<Class<?>, List<PackHandler>> packHandlers = Maps.newHashMap();
    private final Scheduler scheduler = Schedulers.newFocessScheduler(Main.getMainPlugin());
    private final Queue<Packet> packets = Queues.newConcurrentLinkedQueue();

    public FocessSidedClientReceiver(FocessSidedClientSocket focessSidedClientSocket, String name) {
        this.host = focessSidedClientSocket.getHost();
        this.port = focessSidedClientSocket.getPort();
        this.name = name;
        this.focessSidedClientSocket = focessSidedClientSocket;
        scheduler.runTimer(()->{
            if (connected)
                packets.offer(new HeartPacket(id,token,System.currentTimeMillis()));
            else
                focessSidedClientSocket.sendPacket(new SidedConnectPacket(name));
        }, Duration.ZERO,Duration.ofSeconds(2));
        scheduler.runTimer(()->{
            if (connected) {
                Packet packet = packets.poll();
                if (packet == null)
                    packet = new WaitPacket(this.id,this.token);
                focessSidedClientSocket.sendPacket(packet);
            }
        },Duration.ZERO,Duration.ofMillis(100));
    }

    @Override
    public void sendPacket(Packet packet) {
        this.packets.add(new ClientPackPacket(this.id,this.token,packet));
    }

    @PacketHandler
    public void onConnected(ConnectedPacket packet) {
        if (this.connected)
            return;
        this.token = packet.getToken();
        this.id = packet.getClientId();
        this.connected = true;
    }

    @PacketHandler
    public void onDisconnected(DisconnectedPacket packet) {
        this.connected = false;
        focessSidedClientSocket.sendPacket(new SidedConnectPacket(name));
    }

    @PacketHandler
    public void onServerPacket(ServerPackPacket packet) {
        for (PackHandler packHandler : this.packHandlers.getOrDefault(packet.getPacket().getClass(), Lists.newArrayList()))
            packHandler.handle(packet.getPacket());
    }

    @Override
    public <T extends Packet> void registerPackHandler(Class<T> c, PackHandler<T> packHandler) {
        this.packHandlers.compute(c,(k,v)->{
            if (v == null)
                v = Lists.newArrayList();
            v.add(packHandler);
            return v;
        });
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean isConnected() {
        return connected;
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
        return name;
    }

    @Override
    public void close() {
        scheduler.close();
    }
}
