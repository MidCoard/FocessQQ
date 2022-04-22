package top.focess.qq.api.net;

import org.jetbrains.annotations.UnmodifiableView;
import top.focess.qq.api.net.packet.Packet;

import java.util.List;

/**
 * The socket multi receiver for server.
 */
public interface ServerMultiReceiver extends ServerReceiver {

    /**
     * Send packet to the special client
     *
     * @param id     the client id
     * @param packet the packet
     */
    void sendPacket(int id, Packet packet);

    /**
     * Get the list of the clients with given name
     *
     * @param name the client name
     * @return the list of the clients with given name
     */
    @UnmodifiableView
    List<Client> getClients(String name);
}
