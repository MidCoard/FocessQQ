package top.focess.qq.core.net;

import top.focess.qq.Main;
import top.focess.qq.api.exceptions.IllegalPortException;
import com.focess.api.net.*;
import top.focess.qq.api.net.*;
import top.focess.qq.api.net.packet.Packet;
import top.focess.qq.api.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

public class FocessSocket implements Socket {

    private final Map<Class<? extends Packet>,List<Pair<Receiver,Method>>> packetMethods = Maps.newHashMap();
    private final List<Receiver> receivers = Lists.newArrayList();
    private final Thread thread;
    private final ServerSocket server;
    private final int localPort;

    public FocessSocket(int localPort) throws IllegalPortException{
        this.localPort = localPort;
        try {
            this.server = new ServerSocket(localPort);
        } catch (IOException e) {
            throw new IllegalPortException(localPort);
        }
        thread = new Thread(() -> {
            Main.getLogger().debugLang("start-focess-socket",localPort);
            while (true)
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
                                Main.getLogger().thrLang("exception-handle-packet", e);
                            }
                        }
                } catch (IOException e) {
                    Main.getLogger().thrLang("exception-focess-socket",e);
                    if (this.server.isClosed())
                        return;
                }
        });
        thread.start();
    }

    private boolean serverSide = false;
    private boolean clientSide = false;

    public void registerReceiver(Receiver receiver) {
        this.receivers.add(receiver);
        if (receiver instanceof ServerReceiver)
            serverSide = true;
        if (receiver instanceof ClientReceiver)
            clientSide = true;
        for (Method method : receiver.getClass().getDeclaredMethods()) {
            PacketHandler handler;
            if ((handler = method.getAnnotation(PacketHandler.class)) != null) {
                if (method.getParameterTypes().length == 1) {
                    Class<?> packetClass = method.getParameterTypes()[0];
                    if (Packet.class.isAssignableFrom(packetClass) && !Modifier.isAbstract(packetClass.getModifiers())) {
                        try {
                            packetMethods.compute((Class<? extends Packet>) packetClass,(k, v)->{
                                if (v == null)
                                    v = Lists.newArrayList();
                                v.add(Pair.of(receiver,method));
                                return v;
                            });
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
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
                Main.getLogger().thrLang("exception-send-packet",e);
                return false;
            }
        return false;
    }

    public void close() {
        try {
            this.server.close();
        } catch (IOException ignored) {
        }
        this.thread.stop();
        for (Receiver receiver : receivers)
            receiver.close();
    }

    public int getLocalPort() {
        return localPort;
    }

}
