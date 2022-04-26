package top.focess.qq.api.bot;

import net.mamoe.mirai.utils.BotConfiguration;

public enum BotProtocol {

    ANDROID_PHONE(BotConfiguration.MiraiProtocol.ANDROID_PHONE),
    ANDROID_PAD(BotConfiguration.MiraiProtocol.ANDROID_PAD),
    ANDROID_WATCH(BotConfiguration.MiraiProtocol.ANDROID_WATCH),
    IPAD(BotConfiguration.MiraiProtocol.IPAD),
    MACOS(BotConfiguration.MiraiProtocol.MACOS);

    private final BotConfiguration.MiraiProtocol nativeProtocol;

    BotProtocol(BotConfiguration.MiraiProtocol nativeProtocol) {
        this.nativeProtocol = nativeProtocol;
    }

    public BotConfiguration.MiraiProtocol getNativeProtocol() {
        return nativeProtocol;
    }
}
