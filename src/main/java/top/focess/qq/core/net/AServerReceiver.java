package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.net.Client;
import top.focess.qq.api.net.PackHandler;
import top.focess.qq.api.net.ServerReceiver;
import top.focess.qq.api.net.packet.Packet;
import top.focess.qq.api.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class AServerReceiver implements ServerReceiver {


    protected final Map<Integer,Long> lastHeart = Maps.newConcurrentMap();
    protected int defaultClientId = 0;
    protected final Map<Integer, SimpleClient> clientInfos = Maps.newConcurrentMap();
    protected final Map<Plugin,Map<String, Map<Class<?>, List<PackHandler>>>> packHandlers = Maps.newHashMap();

    @Override
    public boolean isConnected(String client) {
        return clientInfos.values().stream().anyMatch(simpleClient -> simpleClient.getName().equals(client));
    }

    @Override
    public @Nullable
    Client getClient(String name) {
        return clientInfos.values().stream().filter(simpleClient -> simpleClient.getName().equals(name)).findFirst().orElse(null);
    }

    protected static String generateToken() {
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

    @Override
    public <T extends Packet> void registerPackHandler(Plugin plugin, String name, Class<T> c, PackHandler<T> packHandler){
        packHandlers.compute(plugin, (k, v) -> {
            if (v == null)
                v = Maps.newHashMap();
            v.compute(name, (k1, v1) -> {
                if (v1 == null)
                    v1 = Maps.newHashMap();
                v1.compute(c, (k2, v2) -> {
                    if (v2 == null)
                        v2 = Lists.newArrayList();
                    v2.add(packHandler);
                    return v2;
                });
                return v1;
            });
            return v;
        });
    }

}
