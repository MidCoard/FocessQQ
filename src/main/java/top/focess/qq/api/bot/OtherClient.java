package top.focess.qq.api.bot;

public interface OtherClient extends Contact{

    String getDeviceKind();

    int getAppId();

    default String getDeviceName() {
        return this.getName();
    }


}
