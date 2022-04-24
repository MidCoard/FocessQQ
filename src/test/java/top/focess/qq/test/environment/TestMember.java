package top.focess.qq.test.environment;

import top.focess.command.CommandPermission;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Group;
import top.focess.qq.api.bot.contact.Member;

import java.util.UUID;

public class TestMember implements Member {
    private final long id;
    private final TestGroup group;
    private final String name;
    private CommandPermission permission;

    public TestMember(long id, TestGroup group) {
        this.id = id;
        this.group = group;
        this.permission = CommandPermission.MEMBER;
        this.name = UUID.randomUUID().toString().substring(0,8);
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
        return this.group.getBot();
    }

    @Override
    public String getRawName() {
        return this.name;
    }

    @Override
    public String getCardName() {
        return this.name;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public CommandPermission getPermission() {
        return this.permission;
    }

    public void setPermission(CommandPermission permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestMember that = (TestMember) o;

        if (id != that.id) return false;
        return group.equals(that.group);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + group.hashCode();
        return result;
    }
}
