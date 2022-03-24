package top.focess.qq.api.bot.contact;

public interface OtherClient extends Contact{

    String getDeviceKind();

    int getAppId();

    default String getDeviceName() {
        return this.getName();
    }


}
