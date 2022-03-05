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

public class FocessReceiver extends AServerReceiver {

    private final FocessSocket focessSocket;
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin());

    public FocessReceiver(FocessSocket focessSocket) {
        this.focessSocket = focessSocket;
        scheduler.runTimer(()->{
            for (SimpleClient simpleClient : clientInfos.values()) {
                long time = lastHeart.getOrDefault(simpleClient.getId(),0L);
                if (System.currentTimeMillis() - time > 10 * 1000)
                    clientInfos.remove(simpleClient.getId());
            }
        }, Duration.ZERO,Duration.ofSeconds(1));
    }

    @PacketHandler
    public void onConnect(ConnectPacket packet) {
        for (SimpleClient simpleClient : clientInfos.values())
            if (simpleClient.getName().equals(packet.getName()))
                return;
        SimpleClient simpleClient = new SimpleClient(packet.getHost(), packet.getPort(), defaultClientId++,packet.getName(),generateToken());
        lastHeart.put(simpleClient.getId(),System.currentTimeMillis());
        clientInfos.put(simpleClient.getId(), simpleClient);
        focessSocket.sendPacket(packet.getHost(),packet.getPort(),new ConnectedPacket(simpleClient.getId(), simpleClient.getToken()));
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
            if (simpleClient.getToken().equals(packet.getToken()) && System.currentTimeMillis() + 5 * 1000> packet.getTime())
                lastHeart.put(simpleClient.getId(),packet.getTime());
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

    private void disconnect(int clientId) {
        SimpleClient simpleClient = clientInfos.remove(clientId);
        if (simpleClient != null)
            focessSocket.sendPacket(simpleClient.getHost(), simpleClient.getPort(),new DisconnectedPacket());
    }

    @Override
    public void sendPacket(String client, Packet packet) {
        for (SimpleClient simpleClient : clientInfos.values())
            if (simpleClient.getName().equals(client))
                this.focessSocket.sendPacket(simpleClient.getHost(), simpleClient.getPort(),new ServerPackPacket(packet));
    }

    @Override
    public void close() {
        scheduler.close();
        for (Integer id : clientInfos.keySet())
            disconnect(id);
    }
}
