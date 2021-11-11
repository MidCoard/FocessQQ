package com.focess.core.net;

import com.focess.api.annotation.PacketHandler;
import com.focess.api.net.PackHandler;
import com.focess.api.net.ServerReceiver;
import com.focess.api.net.packet.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FocessReceiver implements ServerReceiver {

    private final Map<Integer, ClientInfo> clientInfos = Maps.newConcurrentMap();
    private final Map<Integer,Long> lastHeart = Maps.newConcurrentMap();
    private final Map<String, Map<Class<?>,List<PackHandler>>> packHandlers = Maps.newHashMap();
    private int defaultClientId = 0;
    private final FocessSocket focessSocket;
    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    public FocessReceiver(FocessSocket focessSocket) {
        this.focessSocket = focessSocket;
        scheduledThreadPool.scheduleAtFixedRate(()->{
            for (ClientInfo clientInfo : clientInfos.values()) {
                long time = lastHeart.getOrDefault(clientInfo.getId(),0L);
                if (System.currentTimeMillis() - time > 10 * 1000)
                    disconnect(clientInfo.getId());
            }
        },0,1, TimeUnit.SECONDS);
    }

    @PacketHandler
    public void onConnect(ConnectPacket packet) {
        for (ClientInfo clientInfo : clientInfos.values())
            if (clientInfo.getName().equals(packet.getName()))
                return;
        ClientInfo clientInfo = new ClientInfo(packet.getHost(), packet.getPort(), defaultClientId++,packet.getName(),generateToken());
        lastHeart.put(clientInfo.getId(),System.currentTimeMillis());
        clientInfos.put(clientInfo.getId(),clientInfo);
        focessSocket.sendPacket(packet.getHost(),packet.getPort(),new ConnectedPacket(clientInfo.getId(),clientInfo.getToken()));
    }

    @PacketHandler
    public void onDisconnect(DisconnectPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            ClientInfo clientInfo = clientInfos.get(packet.getClientId());
            if (clientInfo.getToken().equals(packet.getToken()))
                disconnect(packet.getClientId());
        }
    }

    @PacketHandler
    public void onHeart(HeartPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            ClientInfo clientInfo = clientInfos.get(packet.getClientId());
            if (clientInfo.getToken().equals(packet.getToken()) && System.currentTimeMillis() + 5 * 1000> packet.getTime())
                lastHeart.put(clientInfo.getId(),packet.getTime());
        }
    }

    @PacketHandler
    public void onClientPacket(ClientPackPacket packet) {
        if (clientInfos.get(packet.getClientId()) != null) {
            ClientInfo clientInfo = clientInfos.get(packet.getClientId());
            if (clientInfo.getToken().equals(packet.getToken()))
                for (PackHandler packHandler : packHandlers.getOrDefault(clientInfo.getName(), Maps.newHashMap()).getOrDefault(packet.getPacket().getClass(),Lists.newArrayList()))
                    packHandler.handle(packet.getPacket());
        }
    }

    private void disconnect(int clientId) {
        ClientInfo clientInfo = clientInfos.remove(clientId);
        if (clientInfo != null)
            focessSocket.sendPacket(clientInfo.getHost(), clientInfo.getPort(),new DisconnectedPacket());
    }

    private static String generateToken() {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0;i<64;i++) {
            switch (random.nextInt(3)) {
                case 0:
                    stringBuilder.append((char)('0' + random.nextInt(10)));
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
    public void sendPacket(String client, Packet packet) {
        for (ClientInfo clientInfo : clientInfos.values())
            if (clientInfo.getName().equals(client))
                this.focessSocket.sendPacket(clientInfo.getHost(),clientInfo.getPort(),new ServerPackPacket(packet));
    }

    @Override
    public void close() {
        this.scheduledThreadPool.shutdownNow();
    }
}
