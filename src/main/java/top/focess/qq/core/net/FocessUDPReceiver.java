package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.packet.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.time.Duration;

public class FocessUDPReceiver extends AServerReceiver {

    private final FocessUDPSocket focessUDPSocket;
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "FocessUDPReceiver");

    public FocessUDPReceiver(final FocessUDPSocket focessUDPSocket) {
        this.focessUDPSocket = focessUDPSocket;
        this.scheduler.runTimer(() -> {
            for (final SimpleClient simpleClient : this.clientInfos.values()) {
                final long time = this.lastHeart.getOrDefault(simpleClient.getId(), 0L);
                if (System.currentTimeMillis() - time > 10 * 1000)
                    this.clientInfos.remove(simpleClient.getId());
            }
        }, Duration.ZERO, Duration.ofSeconds(1));
    }

    private void disconnect(final int clientId) {
        final SimpleClient simpleClient = this.clientInfos.remove(clientId);
        if (simpleClient != null)
            this.focessUDPSocket.sendPacket(simpleClient.getHost(), simpleClient.getPort(), new DisconnectedPacket());
    }

    @Override
    public boolean close() {
        this.scheduler.close();
        for (final Integer id : this.clientInfos.keySet())
            this.disconnect(id);
        return this.unregisterAll();
    }

    @PacketHandler
    public void onConnect(final ConnectPacket packet) {
        for (final SimpleClient simpleClient : this.clientInfos.values())
            if (simpleClient.getName().equals(packet.getName()))
                return;
        final SimpleClient simpleClient = new SimpleClient(packet.getHost(), packet.getPort(), this.defaultClientId++, packet.getName(), generateToken());
        this.lastHeart.put(simpleClient.getId(), System.currentTimeMillis());
        this.clientInfos.put(simpleClient.getId(), simpleClient);
        this.focessUDPSocket.sendPacket(packet.getHost(), packet.getPort(), new ConnectedPacket(simpleClient.getId(), simpleClient.getToken()));
    }

    @PacketHandler
    public void onDisconnect(final DisconnectPacket packet) {
        if (this.clientInfos.get(packet.getClientId()) != null) {
            final SimpleClient simpleClient = this.clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                this.disconnect(packet.getClientId());
        }
    }

    @PacketHandler
    public void onHeart(final HeartPacket packet) {
        if (this.clientInfos.get(packet.getClientId()) != null) {
            final SimpleClient simpleClient = this.clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                this.lastHeart.put(simpleClient.getId(), System.currentTimeMillis());
        }
    }

    @PacketHandler
    public void onClientPacket(final ClientPackPacket packet) {
        if (this.clientInfos.get(packet.getClientId()) != null) {
            final SimpleClient simpleClient = this.clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                for (final Plugin plugin : this.packHandlers.keySet())
                    for (final PackHandler packHandler : this.packHandlers.get(plugin).getOrDefault(simpleClient.getName(), Maps.newHashMap()).getOrDefault(packet.getPacket().getClass(), Lists.newArrayList()))
                        packHandler.handle(packet.getPacket());
        }
    }

    @Override
    public void sendPacket(final String client, final Packet packet) {
        for (final SimpleClient simpleClient : this.clientInfos.values())
            if (simpleClient.getName().equals(client))
                this.focessUDPSocket.sendPacket(simpleClient.getHost(), simpleClient.getPort(), new ServerPackPacket(packet));
    }

}
