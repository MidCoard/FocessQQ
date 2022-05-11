package top.focess.qq.core.bot.mirai.message;

import net.mamoe.mirai.message.data.MessageChain;

import java.util.Objects;
import java.util.stream.Collectors;

public class MiraiMessageChain extends MiraiMessage{
    public MiraiMessageChain(MessageChain message) {
        super(message);
    }

    @Override
    public String toString() {
        return ((MessageChain)this.getMessage()).stream().map(Objects::toString).collect(Collectors.joining("\n"));
    }
}
