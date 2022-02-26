package com.focess.core.net;

import com.focess.Main;
import com.focess.api.net.Socket;
import com.focess.api.net.PacketPreCodec;
import com.focess.api.net.Receiver;
import com.focess.api.net.packet.Packet;
import com.focess.api.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class FocessSidedClientSocket implements Socket {


    private final Map<Class<? extends Packet>, List<Pair<Receiver,Method>>> packetMethods = Maps.newHashMap();
    private final List<Receiver> receivers = Lists.newArrayList();
    private final String host;
    private final int port;

    public FocessSidedClientSocket(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public <T extends Packet> boolean sendPacket(T packet) {
        PacketPreCodec packetPreCodec = new PacketPreCodec();
        if (packetPreCodec.writePacket(packet))
            try {
                java.net.Socket socket = new java.net.Socket(host, port);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(packetPreCodec.getBytes());
                outputStream.flush();
                socket.shutdownOutput();
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int length;
                PacketPreCodec codec = new PacketPreCodec();
                while ((length = inputStream.read(buffer)) != -1)
                    codec.push(buffer, length);
                Packet p = codec.readPacket();
                if (p != null)
                    for (Pair<Receiver, Method> pair : packetMethods.getOrDefault(p.getClass(), Lists.newArrayList())) {
                        Method method = pair.getValue();
                        try {
                            method.setAccessible(true);
                            method.invoke(pair.getKey(), p);
                        } catch (Exception e) {
                            Main.getLogger().thrLang("exception-handle-packet", e);
                        }
                    }
                return true;
            } catch (IOException e) {
                return false;
            }
        return false;
    }

    public void registerReceiver(Receiver receiver) {
        this.receivers.add(receiver);
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
        return false;
    }

    @Override
    public boolean containsClientSide() {
        return true;
    }

    @Override
    public void close() {
        for (Receiver receiver : this.receivers)
            receiver.close();
    }
}
