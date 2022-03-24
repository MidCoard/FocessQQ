package top.focess.qq.core.bot.contact;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Member;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        return GROUP_MAP.computeIfAbsent(bot.getId(), k -> Maps.newConcurrentMap()).computeIfAbsent(group.getId(), k -> new SimpleGroup(bot, group));
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
        return Objects.requireNonNull(SimpleMember.get(this,this.nativeGroup.getOrFail(id)));
    }

    @Override
    public @NonNull List<Member> getMembers() {
        return this.nativeGroup.getMembers().stream().map(i -> SimpleMember.get(this,i)).collect(Collectors.toList());
    }

    @Override
    public Member getAsMember() {
        return Objects.requireNonNull(SimpleMember.get(this,this.nativeGroup.getBotAsMember()));
    }

}
