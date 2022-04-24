package top.focess.qq.test.environment;

import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.Friend;
import top.focess.qq.api.bot.message.Message;

import java.util.UUID;

public class TestFriend implements Friend {
    private final long id;
    private final TestBot bot;
    private final String name;

    public TestFriend(long id, TestBot bot) {
        this.id = id;
        this.bot = bot;
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
        return this.bot;
    }

    @Override
    public String getRawName() {
        return this.name;
    }

    @Override
    public @NotNull String getAvatarUrl() {
        return name + " avatar url";
    }

    @Override
    public void delete() {
        this.bot.removeFriend(this);
    }

    @Override
    public void sendMessage(String message) {
        System.out.println(this.bot.getId() + " send message to friend " + this.name + "(" + this.id + "): " + message);
    }

    @Override
    public void sendMessage(Message message) {
        System.out.println(this.bot.getId() + " send message to friend " + this.name + "(" + this.id + "): " + message.toString());
    }

    @Override
    public Image uploadImage(ExternalResource resource) {
        throw new UnsupportedOperationException();
    }
}
