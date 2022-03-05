package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.packet.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.time.Duration;

public class FocessClientReceiver extends AClientReceiver {

    private final String localhost;
    private final FocessSocket focessSocket;
    private volatile boolean connected = false;
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin());

    public FocessClientReceiver(FocessSocket focessSocket,String localhost,String host,int port,String name) {
        super(host,port,name);
        this.localhost = localhost;
        this.focessSocket = focessSocket;
        scheduler.runTimer(()->{
            if (connected)
                focessSocket.sendPacket(host,port,new HeartPacket(id,token,System.currentTimeMillis()));
            else
                focessSocket.sendPacket(this.host,this.port,new ConnectPacket(localhost,focessSocket.getLocalPort(),name));
        },Duration.ZERO, Duration.ofSeconds(2));
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
        for (Plugin plugin : this.packHandlers.keySet())
            for (PackHandler packHandler : this.packHandlers.get(plugin).getOrDefault(packet.getPacket().getClass(),Lists.newArrayList()))
                packHandler.handle(packet.getPacket());
    }

    @Override
    public void sendPacket(Packet packet) {
        focessSocket.sendPacket(this.host,this.port,new ClientPackPacket(this.id,this.token,packet));
    }

    @Override
    public void close() {
        this.scheduler.close();
    }
}
