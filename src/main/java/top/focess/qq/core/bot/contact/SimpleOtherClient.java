package top.focess.qq.core.bot.contact;

import top.focess.qq.api.bot.Bot;
import top.focess.qq.api.bot.contact.OtherClient;

public class SimpleOtherClient extends SimpleContact implements OtherClient {

    private final String name;
    private final String deviceKind;

    private final int appId;

    public SimpleOtherClient(final Bot bot, final long id, final String name, final String deviceKind, final int appId) {
        super(bot, id);
        this.name = name;
        this.deviceKind = deviceKind;
        this.appId = appId;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDeviceKind() {
        return deviceKind;
    }

    @Override
    public int getAppId() {
        return appId;
    }

}
