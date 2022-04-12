package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.net.IllegalPortException;
import top.focess.qq.api.net.PacketPreCodec;
import top.focess.qq.api.net.Receiver;
import top.focess.qq.api.net.ServerReceiver;
import top.focess.qq.api.net.packet.Packet;
import top.focess.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;

public class FocessSidedSocket extends ASocket {

    private final int localPort;
    private final ServerSocket server;

    public FocessSidedSocket(final int localPort) throws IllegalPortException {
        this.localPort = localPort;
        try {
            this.server = new ServerSocket(localPort);
        } catch (final IOException e) {
            throw new IllegalPortException(localPort);
        }
        final Thread thread = new Thread(() -> {
            FocessQQ.getLogger().debugLang("start-focess-sided-socket", localPort);
            while (!this.server.isClosed())
                try {
                    final java.net.Socket socket = this.server.accept();
                    final InputStream inputStream = socket.getInputStream();
                    final byte[] buffer = new byte[1024];
                    final PacketPreCodec packetPreCodec = new PacketPreCodec();
                    int length;
                    while ((length = inputStream.read(buffer)) != -1)
                        packetPreCodec.push(buffer, length);
                    final Packet packet = packetPreCodec.readPacket();
                    final OutputStream outputStream = socket.getOutputStream();
                    if (packet != null)
                        for (final Pair<Receiver, Method> pair : this.packetMethods.getOrDefault(packet.getClass(), Lists.newArrayList())) {
                            final Method method = pair.getValue();
                            try {
                                method.setAccessible(true);
                                final Object o = method.invoke(pair.getKey(), packet);
                                if (o != null) {
                                    final PacketPreCodec handler = new PacketPreCodec();
                                    handler.writePacket((Packet) o);
                                    outputStream.write(handler.getBytes());
                                    outputStream.flush();
                                }
                            } catch (final Exception e) {
                                FocessQQ.getLogger().thrLang("exception-handle-packet", e);
                            }
                        }
                    socket.shutdownOutput();
                } catch (final IOException e) {
                    FocessQQ.getLogger().thrLang("exception-focess-sided-socket", e);
                    if (this.server.isClosed())
                        return;
                }
        });
        thread.start();
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

    @Override
    public void registerReceiver(final Receiver receiver) {
        if (!(receiver instanceof ServerReceiver))
            throw new UnsupportedOperationException();
        super.registerReceiver(receiver);
    }

    @Override
    public boolean containsServerSide() {
        return true;
    }

    @Override
    public boolean containsClientSide() {
        return false;
    }

    public int getLocalPort() {
        return this.localPort;
    }
}
