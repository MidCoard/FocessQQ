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

    public FocessSocket(int localPort) throws IllegalPortException{
        this.localPort = localPort;
        try {
            this.server = new ServerSocket(localPort);
        } catch (IOException e) {
            throw new IllegalPortException(localPort);
        }
        Thread thread = new Thread(() -> {
            FocessQQ.getLogger().debugLang("start-focess-socket", localPort);
            while (!server.isClosed())
                try {
                    java.net.Socket socket = server.accept();
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    PacketPreCodec packetPreCodec = new PacketPreCodec();
                    int length;
                    while ((length = inputStream.read(buffer)) != -1)
                        packetPreCodec.push(buffer, length);
                    inputStream.close();
                    Packet packet = packetPreCodec.readPacket();
                    if (packet != null)
                        for (Pair<Receiver, Method> pair : packetMethods.getOrDefault(packet.getClass(), Lists.newArrayList())) {
                            Method method = pair.getValue();
                            try {
                                method.setAccessible(true);
                                method.invoke(pair.getKey(), packet);
                            } catch (Exception e) {
                                FocessQQ.getLogger().thrLang("exception-handle-packet", e);
                            }
                        }
                } catch (IOException e) {
                    FocessQQ.getLogger().thrLang("exception-focess-socket", e);
                    if (this.server.isClosed())
                        return;
                }
        });
        thread.start();
    }

    private boolean serverSide = false;
    private boolean clientSide = false;

    public void registerReceiver(Receiver receiver) {
        if (receiver instanceof ServerReceiver)
            serverSide = true;
        if (receiver instanceof ClientReceiver)
            clientSide = true;
        super.registerReceiver(receiver);
    }

    @Override
    public boolean containsServerSide() {
        return serverSide;
    }

    @Override
    public boolean containsClientSide() {
        return clientSide;
    }

    public <T extends Packet> boolean sendPacket(String targetHost,int targetPort,T packet) {
        PacketPreCodec packetPreCodec = new PacketPreCodec();
        if (packetPreCodec.writePacket(packet))
            try {
                java.net.Socket socket = new java.net.Socket(targetHost, targetPort);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(packetPreCodec.getBytes());
                outputStream.flush();
                outputStream.close();
                return true;
            } catch (IOException e) {
                FocessQQ.getLogger().thrLang("exception-send-packet",e);
                return false;
            }
        return false;
    }

    @Override
    public boolean close() {
        boolean ret = false;
        for (Receiver receiver : receivers)
            ret = ret || receiver.close();
        try {
            this.server.close();
        } catch (IOException ignored) {
        }
        return ret;
    }

    public int getLocalPort() {
        return localPort;
    }

}
