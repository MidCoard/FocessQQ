package top.focess.qq.core.bot;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Group;
import top.focess.qq.api.bot.Member;

import java.util.Map;

public class SimpleGroup extends SimpleSpeaker implements Group {

    private static final Map<Long, Map<Long,SimpleGroup>> GROUP_MAP = Maps.newConcurrentMap();

    private final net.mamoe.mirai.contact.Group nativeGroup;

    private SimpleGroup(Bot bot, net.mamoe.mirai.contact.Group nativeGroup) {
        super(bot, nativeGroup);
        this.nativeGroup = nativeGroup;
    }

    @Nullable
    public static Group get(Bot bot, @Nullable net.mamoe.mirai.contact.Group group) {
        if (group == null)
            return null;
        if (bot.getId() != group.getBot().getId())
            return null;
        Map<Long,SimpleGroup> map = GROUP_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap());
        return map.computeIfAbsent(group.getId(), k -> new SimpleGroup(bot, group));
    }

    public static void remove(Bot bot) {
        GROUP_MAP.remove(bot.getId());
    }

    @Override
    public String getName() {
        return this.nativeGroup.getName();
    }

    @Override
    public void quit() {
        this.nativeGroup.quit();
    }

    @Nullable
    @Override
    public Member getMember(long id) {
        return SimpleMember.get(this,this.nativeGroup.get(id));
    }

    @Override
    public Member getMemberOrFail(long id) {
        return SimpleMember.get(this,this.nativeGroup.getOrFail(id));
    }
}
