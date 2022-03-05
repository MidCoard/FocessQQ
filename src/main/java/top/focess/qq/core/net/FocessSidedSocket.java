package top.focess.qq.core.net;

import com.google.common.collect.Lists;
import top.focess.qq.FocessQQ;
import top.focess.qq.api.exceptions.IllegalPortException;
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

public class FocessSidedSocket extends ASocket {

    private final int localPort;
    private final ServerSocket server;
    private final Thread thread;

    public FocessSidedSocket(int localPort) throws IllegalPortException {
        this.localPort = localPort;
        try {
            this.server = new ServerSocket(localPort);
        } catch (IOException e) {
            throw new IllegalPortException(localPort);
        }
        thread = new Thread(() -> {
            FocessQQ.getLogger().debugLang("start-focess-sided-socket",localPort);
            while (!server.isClosed())
                try {
                    java.net.Socket socket = server.accept();
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    PacketPreCodec packetPreCodec = new PacketPreCodec();
                    int length;
                    while ((length = inputStream.read(buffer)) != -1)
                        packetPreCodec.push(buffer, length);
                    Packet packet = packetPreCodec.readPacket();
                    OutputStream outputStream = socket.getOutputStream();
                    if (packet != null)
                        for (Pair<Receiver, Method> pair : packetMethods.getOrDefault(packet.getClass(), Lists.newArrayList())) {
                            Method method = pair.getValue();
                            try {
                                method.setAccessible(true);
                                Object o = method.invoke(pair.getKey(), packet);
                                if (o != null) {
                                    PacketPreCodec handler = new PacketPreCodec();
                                    handler.writePacket((Packet)o);
                                    outputStream.write(handler.getBytes());
                                    outputStream.flush();
                                }
                            } catch (Exception e) {
                                FocessQQ.getLogger().thrLang("exception-handle-packet", e);
                            }
                        }
                    socket.shutdownOutput();
                } catch (IOException e) {
                    FocessQQ.getLogger().thrLang("exception-focess-sided-socket",e);
                    if (this.server.isClosed())
                        return;
                }
        });
        thread.start();
    }

    public void close() {
        try {
            this.server.close();
        } catch (IOException ignored) {
        }
        for (Receiver receiver : receivers)
            receiver.close();
    }

    public void registerReceiver(Receiver receiver) {
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
        return localPort;
    }
}
