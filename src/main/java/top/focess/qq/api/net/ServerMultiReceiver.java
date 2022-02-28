package top.focess.qq.api.net;

import top.focess.qq.api.net.packet.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The socket multi receiver for server.
 */
public interface ServerMultiReceiver extends ServerReceiver{

    /**
     * Send packet to the special client
     * @param id the client id
     * @param packet the packet
     */
    void sendPacket(int id, Packet packet);

    /**
     * Get the list of the clients with given name
     *
     * @param name the client name
     * @return the list of the clients with given name
     */
    List<Client> getClients(String name);

    @Override
    @Nullable
    default Client getClient(String name) {
        return getClients(name).stream().findAny().orElse(null);
    }
}
