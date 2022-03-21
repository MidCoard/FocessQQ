package top.focess.qq.core.bot;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Stranger;

import java.util.Map;

public class SimpleStranger extends SimpleSpeaker implements Stranger {

    private static final Map<Long, Map<Long,SimpleStranger>> STRANGER_MAP = Maps.newConcurrentMap();

    private final net.mamoe.mirai.contact.Stranger stranger;

    @Nullable
    public static SimpleStranger get(Bot bot, net.mamoe.mirai.contact.Stranger stranger) {
        if (stranger == null)
            return null;
        if (bot.getId() != stranger.getBot().getId())
            return null;
        Map<Long, SimpleStranger> map = STRANGER_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap());
        return map.computeIfAbsent(stranger.getId(), k -> new SimpleStranger(bot, stranger));
    }

    private SimpleStranger(Bot bot, net.mamoe.mirai.contact.Stranger stranger) {
        super(bot, stranger);
        this.stranger = stranger;
    }

    public static void remove(Bot bot) {
        STRANGER_MAP.remove(bot.getId());
    }

    @Override
    public String getName() {
        return this.stranger.getRemark();
    }

    @Override
    public String getRawName() {
        return this.stranger.getNick();
    }
}
