package top.focess.qq.core.bot.contact;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.OtherClient;

import java.util.Map;

public class SimpleOtherClient extends SimpleContact implements OtherClient{


    private static final Map<Long, Map<Long,SimpleOtherClient>> OTHER_CLIENT_MAP = Maps.newConcurrentMap();
    private final net.mamoe.mirai.contact.OtherClient nativeOtherClient;

    public SimpleOtherClient(Bot bot, net.mamoe.mirai.contact.OtherClient contact) {
        super(bot, contact);
        this.nativeOtherClient = contact;
    }

    @Override
    public String getName() {
        return this.nativeOtherClient.getInfo().getDeviceName();
    }

    @Nullable
    public static SimpleOtherClient get(Bot bot, @Nullable net.mamoe.mirai.contact.OtherClient otherClient) {
        if (otherClient == null)
            return null;
        if (otherClient.getBot().getId() != bot.getId())
            return null;
        return OTHER_CLIENT_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap()).computeIfAbsent(otherClient.getId(), k -> new SimpleOtherClient(bot, otherClient));
    }

    @Override
    public String getDeviceKind() {
        return this.nativeOtherClient.getInfo().getDeviceKind();
    }

    @Override
    public int getAppId() {
        return this.nativeOtherClient.getInfo().getAppId();
    }
}
