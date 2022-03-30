package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.IllegalPortException;
import top.focess.qq.api.net.ClientReceiver;
import top.focess.qq.api.net.PacketPreCodec;
import top.focess.qq.api.net.Receiver;
import top.focess.qq.api.net.ServerReceiver;
import top.focess.qq.api.net.packet.Packet;
import top.focess.qq.api.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;

public class FocessSocket extends ASocket {

    private final ServerSocket server;
    private final int localPort;

    public FocessSocket(final int localPort) throws IllegalPortException{
        this.localPort = localPort;
        try {
            this.server = new ServerSocket(localPort);
        } catch (final IOException e) {
            throw new IllegalPortException(localPort);
        }
        final Thread thread = new Thread(() -> {
            FocessQQ.getLogger().debugLang("start-focess-socket", localPort);
            while (!this.server.isClosed())
                try {
                    final java.net.Socket socket = this.server.accept();
                    final InputStream inputStream = socket.getInputStream();
                    final byte[] buffer = new byte[1024];
                    final PacketPreCodec packetPreCodec = new PacketPreCodec();
                    int length;
                    while ((length = inputStream.read(buffer)) != -1)
                        packetPreCodec.push(buffer, length);
                    inputStream.close();
                    final Packet packet = packetPreCodec.readPacket();
                    if (packet != null)
                        for (final Pair<Receiver, Method> pair : this.packetMethods.getOrDefault(packet.getClass(), Lists.newArrayList())) {
                            final Method method = pair.getValue();
                            try {
                                method.setAccessible(true);
                                method.invoke(pair.getKey(), packet);
                            } catch (final Exception e) {
                                FocessQQ.getLogger().thrLang("exception-handle-packet", e);
                            }
                        }
                } catch (final IOException e) {
                    FocessQQ.getLogger().thrLang("exception-focess-socket", e);
                    if (this.server.isClosed())
                        return;
                }
        });
        thread.start();
    }

    private boolean serverSide;
    private boolean clientSide;

    public void registerReceiver(final Receiver receiver) {
        if (receiver instanceof ServerReceiver)
            this.serverSide = true;
        if (receiver instanceof ClientReceiver)
            this.clientSide = true;
        super.registerReceiver(receiver);
    }

    @Override
    public boolean containsServerSide() {
        return this.serverSide;
    }

    @Override
    public boolean containsClientSide() {
        return this.clientSide;
    }

    public <T extends Packet> boolean sendPacket(final String targetHost, final int targetPort, final T packet) {
        final PacketPreCodec packetPreCodec = new PacketPreCodec();
        if (packetPreCodec.writePacket(packet))
            try {
                final java.net.Socket socket = new java.net.Socket(targetHost, targetPort);
                final OutputStream outputStream = socket.getOutputStream();
                outputStream.write(packetPreCodec.getBytes());
                outputStream.flush();
                outputStream.close();
                return true;
            } catch (final IOException e) {
                FocessQQ.getLogger().thrLang("exception-send-packet",e);
                return false;
            }
        return false;
    }

    @Override
    public boolean close() {
        boolean ret = false;
        for (final Receiver receiver : this.receivers)
            ret = ret || receiver.close();
        try {
            this.server.close();
        } catch (final IOException ignored) {
        }
        return ret;
    }

    public int getLocalPort() {
        return this.localPort;
    }

}
