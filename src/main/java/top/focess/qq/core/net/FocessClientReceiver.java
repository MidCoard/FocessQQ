package top.focess.qq.core.net;

import top.focess.qq.api.net.ClientReceiver;
import top.focess.qq.api.net.PackHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.qq.api.net.packet.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FocessClientReceiver implements ClientReceiver {

    private final String host;
    private final int port;
    private final String localhost;
    private final String name;
    private final FocessSocket focessSocket;
    private String token;
    private int id;
    private volatile boolean connected = false;
    private final Map<Class<?>,List<PackHandler>> packHandlers = Maps.newHashMap();
    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);

    public FocessClientReceiver(FocessSocket focessSocket,String localhost,String host,int port,String name) {
        this.host = host;
        this.port = port;
        this.localhost = localhost;
        this.name = name;
        this.focessSocket = focessSocket;
        scheduledThreadPool.scheduleAtFixedRate(()->{
            if (connected)
                focessSocket.sendPacket(host,port,new HeartPacket(id,token,System.currentTimeMillis()));
            else
                focessSocket.sendPacket(this.host,this.port,new ConnectPacket(localhost,focessSocket.getLocalPort(),name));
        },0,2, TimeUnit.SECONDS);
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
        focessSocket.sendPacket(this.host,this.port,new ConnectPacket(this.localhost,focessSocket.getLocalPort(),name));
    }

    @PacketHandler
    public void onServerPacket(ServerPackPacket packet) {
        for (PackHandler packHandler : this.packHandlers.getOrDefault(packet.getPacket().getClass(),Lists.newArrayList()))
            packHandler.handle(packet.getPacket());
    }

    public <T extends Packet> void registerPackHandler(Class<T> c,PackHandler<T> packHandler) {
        this.packHandlers.compute(c,(k,v)->{
            if (v == null)
                v = Lists.newArrayList();
            v.add(packHandler);
            return v;
        });
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
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

    @Override
    public void sendPacket(Packet packet) {
        focessSocket.sendPacket(this.host,this.port,new ClientPackPacket(this.id,this.token,packet));
    }

    @Override
    public void close() {
        scheduledThreadPool.shutdownNow();
    }
}
