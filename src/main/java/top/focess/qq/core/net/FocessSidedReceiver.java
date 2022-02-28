package top.focess.qq.core.net;

import top.focess.qq.api.net.Client;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.ServerReceiver;
import com.focess.api.net.packet.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.packet.*;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FocessSidedReceiver implements ServerReceiver {

    private final Map<Integer, SimpleClient> clientInfos = Maps.newConcurrentMap();
    private final Map<Integer,Long> lastHeart = Maps.newConcurrentMap();
    private final Map<String, Map<Class<?>, List<PackHandler>>> packHandlers = Maps.newHashMap();
    private final Map<String, Queue<Packet>> packets = Maps.newConcurrentMap();
    private int defaultClientId = 0;
    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    public FocessSidedReceiver() {
        scheduledThreadPool.scheduleAtFixedRate(()->{
            for (SimpleClient simpleClient : clientInfos.values()) {
                long time = lastHeart.getOrDefault(simpleClient.getId(),0L);
                if (System.currentTimeMillis() - time > 10 * 1000)
                    clientInfos.remove(simpleClient.getId());
            }
        },0,1, TimeUnit.SECONDS);
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
                for (PackHandler packHandler : packHandlers.getOrDefault(simpleClient.getName(), Maps.newHashMap()).getOrDefault(packet.getPacket().getClass(), Lists.newArrayList()))
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

    private static String generateToken() {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0;i<64;i++) {
            switch (random.nextInt(3)) {
                case 0:
                    stringBuilder.append((char) ('0' + random.nextInt(10)));
                    break;
                case 1:
                    stringBuilder.append((char)('a' + random.nextInt(26)));
                    break;
                case 2:
                    stringBuilder.append((char)('A' + random.nextInt(26)));
                    break;
            }
        }
        return stringBuilder.toString();
    }

    public <T extends Packet> void registerPackHandler(String name,Class<T> c, PackHandler<T> packHandler){
        packHandlers.compute(name,(k, v)->{
            if (v == null)
                v = Maps.newHashMap();
            v.compute(c,(k1,v1)->{
                if (v1 == null)
                    v1 = Lists.newArrayList();
                v1.add(packHandler);
                return v1;
            });
            return v;
        });
    }

    @Override
    public void close() {
        this.scheduledThreadPool.shutdownNow();
        for (Integer id : clientInfos.keySet())
            disconnect(id);
    }

    @Override
    public boolean isConnected(String client) {
        for (Integer id : clientInfos.keySet())
            if (clientInfos.get(id).getName().equals(client))
                return true;
        return false;
    }

    @Override
    public @Nullable Client getClient(String name) {
        for (SimpleClient simpleClient : clientInfos.values())
            if (simpleClient.getName().equals(name))
                return simpleClient;
        return null;
    }
}
