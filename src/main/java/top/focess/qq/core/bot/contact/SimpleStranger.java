package top.focess.qq.core.bot.contact;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Stranger;

import java.util.Map;

public class SimpleStranger extends SimpleTransmitter implements Stranger {

    private static final Map<Long, Map<Long, SimpleStranger>> STRANGER_MAP = Maps.newConcurrentMap();

    private final net.mamoe.mirai.contact.Stranger stranger;

    private SimpleStranger(final Bot bot, final net.mamoe.mirai.contact.Stranger stranger) {
        super(bot, stranger);
        this.stranger = stranger;
    }

    @Nullable
    public static SimpleStranger get(final Bot bot, final net.mamoe.mirai.contact.Stranger stranger) {
        if (stranger == null)
            return null;
        if (bot.getId() != stranger.getBot().getId())
            return null;
        return STRANGER_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap()).computeIfAbsent(stranger.getId(), k -> new SimpleStranger(bot, stranger));
    }

    public static void remove(@NotNull final Bot bot) {
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
