package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.packet.*;
import top.focess.qq.api.plugin.Plugin;
import top.focess.qq.api.schedule.Scheduler;
import top.focess.qq.api.schedule.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Queue;

public class FocessSidedReceiver extends AServerReceiver {

    private final Map<String, Queue<Packet>> packets = Maps.newConcurrentMap();
    private final Scheduler scheduler = Schedulers.newFocessScheduler(FocessQQ.getMainPlugin());

    public FocessSidedReceiver() {
        scheduler.runTimer(()->{
            for (SimpleClient simpleClient : clientInfos.values()) {
                long time = lastHeart.getOrDefault(simpleClient.getId(),0L);
                if (System.currentTimeMillis() - time > 10 * 1000)
                    clientInfos.remove(simpleClient.getId());
            }
        }, Duration.ZERO,Duration.ofSeconds(1));
    }

    @PacketHandler
    public ConnectedPacket onConnect(SidedConnectPacket packet) {
        for (SimpleClient simpleClient : clientInfos.values())
            if (simpleClient.getName().equals(packet.getName()))
                return null;
        SimpleClient simpleClient = new SimpleClient(defaultClientId++,packet.getName(),generateToken());
        lastHeart.put(simpleClient.getId(),System.currentTimeMillis());
        clientInfos.put(simpleClient.getId(), simpleClient);
        return new ConnectedPacket(simpleClient.getId(), simpleClient.getToken());
    }

    @PacketHandler
    public DisconnectedPacket onDisconnect(DisconnectPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                return disconnect(packet.getClientId());
        }
        return null;
    }

    @PacketHandler
    public Packet onHeart(HeartPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()) && System.currentTimeMillis() + 5 * 1000 > packet.getTime()) {
                lastHeart.put(simpleClient.getId(), packet.getTime());
                return packets.getOrDefault(simpleClient.getName(), Queues.newConcurrentLinkedQueue()).poll();
            }
        }
        return null;
    }

    @PacketHandler
    public Packet onClientPacket(ClientPackPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken())) {
                for (Plugin plugin : this.packHandlers.keySet())
                    for (PackHandler packHandler : packHandlers.get(plugin).getOrDefault(simpleClient.getName(), Maps.newHashMap()).getOrDefault(packet.getPacket().getClass(), Lists.newArrayList()))
                        packHandler.handle(packet.getPacket());
                return packets.getOrDefault(simpleClient.getName(), Queues.newConcurrentLinkedQueue()).poll();
            }
        }
        return null;
    }

    @PacketHandler
    public Packet onWait(WaitPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            SimpleClient simpleClient = clientInfos.get(packet.getClientId());
            if (simpleClient.getToken().equals(packet.getToken()))
                return packets.getOrDefault(simpleClient.getName(), Queues.newConcurrentLinkedQueue()).poll();
        }
        return null;
    }

    public void sendPacket(String client,Packet packet) {
        packets.compute(client,(k, v)->{
            if (v == null)
                v = Queues.newConcurrentLinkedQueue();
            v.offer(new ServerPackPacket(packet));
            return v;
        });
    }

    private DisconnectedPacket disconnect(int clientId) {
        clientInfos.remove(clientId);
        return new DisconnectedPacket();
    }

    @Override
    public boolean close() {
        scheduler.close();
        for (Integer id : clientInfos.keySet())
            disconnect(id);
        return this.unregisterAll();
    }
}
