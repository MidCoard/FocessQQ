package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.ClientReceiver;
import top.focess.qq.api.net.PacketPreCodec;
import top.focess.qq.api.net.Receiver;
import top.focess.qq.api.net.packet.Packet;
import top.focess.qq.api.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class FocessSidedClientSocket extends ASocket {

    private final String host;
    private final int port;

    public FocessSidedClientSocket(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public <T extends Packet> boolean sendPacket(final T packet) {
        final PacketPreCodec packetPreCodec = new PacketPreCodec();
        if (packetPreCodec.writePacket(packet))
            try {
                final java.net.Socket socket = new java.net.Socket(this.host, this.port);
                final OutputStream outputStream = socket.getOutputStream();
                outputStream.write(packetPreCodec.getBytes());
                outputStream.flush();
                socket.shutdownOutput();
                final InputStream inputStream = socket.getInputStream();
                final byte[] buffer = new byte[1024];
                int length;
                final PacketPreCodec codec = new PacketPreCodec();
                while ((length = inputStream.read(buffer)) != -1)
                    codec.push(buffer, length);
                final Packet p = codec.readPacket();
                if (p != null)
                    for (final Pair<Receiver, Method> pair : this.packetMethods.getOrDefault(p.getClass(), Lists.newArrayList())) {
                        final Method method = pair.getValue();
                        try {
                            method.setAccessible(true);
                            method.invoke(pair.getKey(), p);
                        } catch (final Exception e) {
                            FocessQQ.getLogger().thrLang("exception-handle-packet", e);
                        }
                    }
                return true;
            } catch (final IOException e) {
                return false;
            }
        return false;
    }

    public void registerReceiver(final Receiver receiver) {
        if (!(receiver instanceof ClientReceiver))
            throw new UnsupportedOperationException();
        super.registerReceiver(receiver);
    }

    @Override
    public boolean containsServerSide() {
        return false;
    }

    @Override
    public boolean containsClientSide() {
        return true;
    }

    @Override
    public boolean close() {
        boolean ret = false;
        for (final Receiver receiver : this.receivers)
            ret = ret || receiver.close();
        return ret;
    }
}
