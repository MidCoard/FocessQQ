package top.focess.qq.api.bot.message.raw;

import top.focess.qq.api.serialize.FocessSerializable;

public abstract class Message implements FocessSerializable {

    public abstract net.mamoe.mirai.message.data.Message toMiraiMessage();
}
