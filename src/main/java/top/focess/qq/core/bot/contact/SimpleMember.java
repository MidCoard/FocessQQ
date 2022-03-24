package top.focess.qq.core.bot.contact;

import com.google.common.collect.Maps;
import net.mamoe.mirai.contact.MemberPermission;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.Group;
import top.focess.qq.api.bot.Member;
import top.focess.qq.api.command.CommandPermission;

import java.util.Map;

import static top.focess.qq.api.command.CommandPermission.*;

public class SimpleMember extends SimpleContact implements Member {

    private static final Map<Long, Map<Long,SimpleMember>> GROUP_MEMBER_MAP = Maps.newConcurrentMap();
    private final net.mamoe.mirai.contact.Member nativeMember;
    private final Group simpleGroup;

    private SimpleMember(Group simpleGroup, net.mamoe.mirai.contact.Member nativeMember) {
        super(simpleGroup.getBot(), nativeMember);
        this.simpleGroup = simpleGroup;
        this.nativeMember = nativeMember;
    }

    @Nullable
    public static Member get(Group group, @Nullable net.mamoe.mirai.contact.Member member) {
        if (member == null)
            return null;
        if (group.getBot().getId() != member.getBot().getId())
            return null;
        return GROUP_MEMBER_MAP.computeIfAbsent(group.getBot().getId(), k -> Maps.newConcurrentMap()).computeIfAbsent(member.getId(), k -> new SimpleMember(group, member));
    }

    @Nullable
    public static Member get(Bot bot, @Nullable net.mamoe.mirai.contact.Member member) {
        if (member == null)
            return null;
        Group group = bot.getGroup(member.getGroup().getId());
        if (group == null)
            return null;
        return get(group, member);
    }

    public static void remove(Bot bot) {
        GROUP_MEMBER_MAP.remove(bot.getId());
    }

    @Override
    public String getName() {
        return this.nativeMember.getRemark();
    }

    @Override
    public String getRawName() {
        return this.nativeMember.getNick();
    }

    @Override
    public String getCardName() {
        return this.nativeMember.getNameCard();
    }

    @Override
    public Group getGroup() {
        return this.simpleGroup;
    }

    @Override
    public CommandPermission getPermission() {
        MemberPermission permission = this.nativeMember.getPermission();
        if (permission == MemberPermission.OWNER)
            return OWNER;
        else if (permission == MemberPermission.ADMINISTRATOR)
            return ADMINISTRATOR;
        else return MEMBER;
    }
}
