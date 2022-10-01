package top.focess.qq.api.net;

import org.jetbrains.annotations.UnmodifiableView;
import top.focess.net.Client;
import top.focess.net.packet.Packet;
import top.focess.qq.core.permission.Permission;
import top.focess.qq.core.permission.PermissionEnv;

import java.util.List;

@PermissionEnv(values = {Permission.SEND_PACKET})
public class ServerMultiReceiver extends ServerReceiver {


    public ServerMultiReceiver(top.focess.net.receiver.ServerMultiReceiver receiver) {
        super(receiver);
    }

    public void sendPacket(int i, Packet packet) {
        Permission.checkPermission(Permission.SEND_PACKET);
        ((top.focess.net.receiver.ServerMultiReceiver)receiver).sendPacket(i,packet);
    }

    public @UnmodifiableView List<Client> getClients(String s) {
        return ((top.focess.net.receiver.ServerMultiReceiver)receiver).getClients(s);
    }

}
