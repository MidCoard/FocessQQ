package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.Client;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.ServerMultiReceiver;
import top.focess.qq.api.net.packet.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Schedulers;
import top.focess.scheduler.Scheduler;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class FocessUDPMultiReceiver extends AServerReceiver implements ServerMultiReceiver {

    private final FocessUDPSocket focessUDPSocket;
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin(), "FocessUDPMultiReceiver");

    public FocessUDPMultiReceiver(final FocessUDPSocket focessUDPSocket) {
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
            this.focessUDPSocket.sendPacket(Objects.requireNonNull(simpleClient.getHost()), simpleClient.getPort(), new DisconnectedPacket());
    }

    @Override
    public boolean close() {
        this.scheduler.close();
        for (final Integer id : this.clientInfos.keySet())
            this.disconnect(id);
        return this.unregisterAll();
    }

    @PacketHandler
    public void onConnect(@NotNull final ConnectPacket packet) {
        final SimpleClient simpleClient = new SimpleClient(packet.getHost(), packet.getPort(), this.defaultClientId++, packet.getName(), generateToken());
        this.lastHeart.put(simpleClient.getId(), System.currentTimeMillis());
        this.clientInfos.put(simpleClient.getId(), simpleClient);
        this.focessUDPSocket.sendPacket(packet.getHost(), packet.getPort(), new ConnectedPacket(simpleClient.getId(), simpleClient.getToken()));
    }

    @PacketHandler
    public void onDisconnect(@NotNull final DisconnectPacket packet) {
        if (this.clientInfos.get(packet.getClientId()) != null) {
            final SimpleClient simpleClient = this.clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                this.disconnect(packet.getClientId());
        }
    }

    @PacketHandler
    public void onHeart(@NotNull final HeartPacket packet) {
        if (this.clientInfos.get(packet.getClientId()) != null) {
            final SimpleClient simpleClient = this.clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                this.lastHeart.put(simpleClient.getId(), System.currentTimeMillis());
        }
    }

    @PacketHandler
    public void onClientPacket(@NotNull final ClientPackPacket packet) {
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
                this.focessUDPSocket.sendPacket(Objects.requireNonNull(simpleClient.getHost()), simpleClient.getPort(), new ServerPackPacket(packet));
    }

    @Override
    public void sendPacket(final int id, final Packet packet) {
        final SimpleClient simpleClient = this.clientInfos.get(id);
        if (simpleClient != null)
            this.focessUDPSocket.sendPacket(Objects.requireNonNull(simpleClient.getHost()), simpleClient.getPort(), packet);
    }

    @Override
    public List<Client> getClients(final String name) {
        final List<Client> ret = Lists.newArrayList();
        for (final SimpleClient client : this.clientInfos.values())
            if (client.getName().equals(name))
                ret.add(client);
        return ret;
    }


}
