package top.focess.qq.api.bot.contact;

/**
 * Represents a client.
 */
public interface OtherClient extends Contact {

    /**
     * The device type of this client
     * @return the device type of this client
     */
    String getDeviceKind();

    /**
     * The device appid of this client
     * @return the device appid of this client
     */
    int getAppId();

    /**
     * The device name of this client
     * @return the device name of this client
     */
    default String getDeviceName() {
        return this.getName();
    }


}
