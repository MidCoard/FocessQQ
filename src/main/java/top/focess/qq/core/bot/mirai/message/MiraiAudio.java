package top.focess.qq.core.bot.mirai.message;

import net.mamoe.mirai.message.data.OfflineAudio;
import top.focess.qq.api.bot.message.Audio;

public class MiraiAudio extends MiraiMessage implements Audio {

    public MiraiAudio(OfflineAudio audio) {
        super(audio);
    }
}
