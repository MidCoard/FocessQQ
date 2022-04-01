package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.jetbrains.annotations.NotNull;
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

    public FocessSidedClientReceiver(@NotNull final FocessSidedClientSocket focessSidedClientSocket, final String name) {
        super(focessSidedClientSocket.getHost(), focessSidedClientSocket.getPort(), name);
        this.focessSidedClientSocket = focessSidedClientSocket;
        this.scheduler.runTimer(() -> {
            if (this.connected)
                this.packets.offer(new HeartPacket(this.id, this.token, System.currentTimeMillis()));
            else
                focessSidedClientSocket.sendPacket(new SidedConnectPacket(name));
        }, Duration.ZERO, Duration.ofSeconds(2));
        this.scheduler.runTimer(() -> {
            if (this.connected) {
                Packet packet = this.packets.poll();
                if (packet == null)
                    packet = new WaitPacket(this.id, this.token);
                focessSidedClientSocket.sendPacket(packet);
            }
        }, Duration.ZERO, Duration.ofMillis(100));
    }

    @Override
    public void sendPacket(final Packet packet) {
        this.packets.add(new ClientPackPacket(this.id, this.token, packet));
    }

    @PacketHandler
    public void onConnected(final ConnectedPacket packet) {
        if (this.connected)
            return;
        this.token = packet.getToken();
        this.id = packet.getClientId();
        this.connected = true;
    }

    @PacketHandler
    public void onDisconnected(final DisconnectedPacket packet) {
        this.connected = false;
        this.focessSidedClientSocket.sendPacket(new SidedConnectPacket(this.name));
    }

    @PacketHandler
    public void onServerPacket(final ServerPackPacket packet) {
        for (final Plugin plugin : this.packHandlers.keySet())
            for (final PackHandler packHandler : this.packHandlers.get(plugin).getOrDefault(packet.getPacket().getClass(), Lists.newArrayList()))
                packHandler.handle(packet.getPacket());
    }

    @Override
    public boolean close() {
        this.scheduler.close();
        return this.unregisterAll();
    }
}
