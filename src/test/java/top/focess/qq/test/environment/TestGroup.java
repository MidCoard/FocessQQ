package top.focess.qq.test.environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import top.focess.command.CommandPermission;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Member;
import top.focess.qq.api.bot.message.Audio;
import top.focess.qq.api.bot.message.Image;
import top.focess.qq.api.bot.message.Message;
import top.focess.qq.core.bot.contact.SimpleMember;

import java.io.InputStream;
import java.util.*;

public class TestGroup implements Group {
    private final long id;
    private final TestBot bot;
    private final String name;

    private final Set<Member> members = Sets.newConcurrentHashSet();

    public TestGroup(long id, TestBot bot) {
        this.id = id;
        this.bot = bot;
        this.name = UUID.randomUUID().toString().substring(0,8);
        Random random = new Random();
        int size = random.nextInt(30) + 1;
        String name = UUID.randomUUID().toString().substring(0,8);
        this.members.add(new SimpleMember(this, random.nextLong(),name,name,name ,CommandPermission.OWNER));
        for (int i = 1; i < size; i++) {
            name = UUID.randomUUID().toString().substring(0,8);
            this.members.add(new SimpleMember(this,random.nextLong(), name,name,name,CommandPermission.MEMBER));
        }
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Bot getBot() {
        return this.bot;
    }

    @Override
    public void quit() {
        this.bot.removeGroup(this);
    }

    @Override
    public @Nullable Member getMember(long id) {
        for (Member member : members)
            if (member.getId() == id)
                return member;
        return null;
    }

    @Override
    public Member getMemberOrFail(long id) {
        for (Member member : members)
            if (member.getId() == id)
                return member;
        throw new NullPointerException();
    }

    @Override
    public @NonNull @UnmodifiableView List<Member> getMembers() {
        return Collections.unmodifiableList(Lists.newArrayList(this.members));
    }

    @Override
    public Member getAsMember() {
        return new SimpleMember(this, this.bot.getId(), "" + this.bot.getId(),"" + this.bot.getId(), "" + this.bot.getId(), CommandPermission.MEMBER);
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(this.bot.getId() + " send message in group " + this.name + "(" + this.id + "): " + message);
    }

    @Override
    public void sendMessage(Message message) {
        System.out.println(this.bot.getId() + " send message in group " + this.name + "(" + this.id + "): " + message.toString());
    }

    @Override
    public Image uploadImage(InputStream resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestGroup testGroup = (TestGroup) o;

        if (id != testGroup.id) return false;
        return bot.equals(testGroup.bot);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + bot.hashCode();
        return result;
    }

    @Override
    public Audio uploadAudio(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }
}
