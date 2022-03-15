package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.packet.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.time.Duration;
import java.util.Queue;

public class FocessSidedClientReceiver extends AClientReceiver {

    private final FocessSidedClientSocket focessSidedClientSocket;
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "FocessSidedClientReceiver");
    private final Queue<Packet> packets = Queues.newConcurrentLinkedQueue();

    public FocessSidedClientReceiver(FocessSidedClientSocket focessSidedClientSocket, String name) {
        super(focessSidedClientSocket.getHost(), focessSidedClientSocket.getPort(),name);
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
        for (Plugin plugin : this.packHandlers.keySet())
            for (PackHandler packHandler : this.packHandlers.get(plugin).getOrDefault(packet.getPacket().getClass(), Lists.newArrayList()))
                packHandler.handle(packet.getPacket());
    }

    @Override
    public boolean close() {
        scheduler.close();
        return this.unregisterAll();
    }
}
