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

public class FocessUDPReceiver extends AServerReceiver{

    private final FocessUDPSocket focessUDPSocket;
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin());

    public FocessUDPReceiver(FocessUDPSocket focessUDPSocket) {
        this.focessUDPSocket = focessUDPSocket;
        scheduler.runTimer(()->{
            for (SimpleClient simpleClient : clientInfos.values()) {
                long time = lastHeart.getOrDefault(simpleClient.getId(),0L);
                if (System.currentTimeMillis() - time > 10 * 1000)
                    clientInfos.remove(simpleClient.getId());
            }
        }, Duration.ZERO,Duration.ofSeconds(1));
    }

    private void disconnect(int clientId) {
        SimpleClient simpleClient = clientInfos.remove(clientId);
        if (simpleClient != null)
            focessUDPSocket.sendPacket(simpleClient.getHost(), simpleClient.getPort(),new DisconnectedPacket());
    }

    @Override
    public boolean close() {
        scheduler.close();
        for (Integer id : clientInfos.keySet())
            disconnect(id);
        return this.unregisterAll();
    }

    @PacketHandler
    public void onConnect(ConnectPacket packet) {
        for (SimpleClient simpleClient : clientInfos.values())
            if (simpleClient.getName().equals(packet.getName()))
                return;
        SimpleClient simpleClient = new SimpleClient(packet.getHost(), packet.getPort(), defaultClientId++,packet.getName(),generateToken());
        lastHeart.put(simpleClient.getId(),System.currentTimeMillis());
        clientInfos.put(simpleClient.getId(), simpleClient);
        focessUDPSocket.sendPacket(packet.getHost(),packet.getPort(),new ConnectedPacket(simpleClient.getId(), simpleClient.getToken()));
    }

    @PacketHandler
    public void onDisconnect(DisconnectPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                disconnect(packet.getClientId());
        }
    }

    @PacketHandler
    public void onHeart(HeartPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                lastHeart.put(simpleClient.getId(),System.currentTimeMillis());
        }
    }

    @PacketHandler
    public void onClientPacket(ClientPackPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                for (Plugin plugin : this.packHandlers.keySet())
                    for (PackHandler packHandler : packHandlers.get(plugin).getOrDefault(simpleClient.getName(), Maps.newHashMap()).getOrDefault(packet.getPacket().getClass(),Lists.newArrayList()))
                        packHandler.handle(packet.getPacket());
        }
    }

    @Override
    public void sendPacket(String client, Packet packet) {
        for (SimpleClient simpleClient : this.clientInfos.values())
            if (simpleClient.getName().equals(client))
                this.focessUDPSocket.sendPacket(simpleClient.getHost(), simpleClient.getPort(),new ServerPackPacket(packet));
    }

}
