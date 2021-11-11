package com.focess.core.net;

import com.focess.Main;
import com.focess.api.annotation.PacketHandler;
import com.focess.api.exceptions.IllegalPortException;
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
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

public class FocessSidedSocket implements Socket {

    private final Map<Class<? extends Packet>, List<Pair<Receiver,Method>>> packetMethods = Maps.newHashMap();
    private final List<Receiver> receivers = Lists.newArrayList();
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
            Main.getLogger().debug("FocessSidedSocket (" + localPort + ") is Ready");
            while (true)
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
                                Main.getLogger().thr("Invoke Packet Exception", e);
                            }
                        }
                    socket.shutdownOutput();
                } catch (IOException e) {
                    e.printStackTrace();
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
        this.thread.stop();
        for (Receiver receiver : receivers)
            receiver.close();
    }

    public void registerReceiver(Receiver receiver) {
        receivers.add(receiver);
        for (Method method : receiver.getClass().getDeclaredMethods()) {
            PacketHandler handler;
            if ((handler = method.getAnnotation(PacketHandler.class)) != null) {
                if (method.getParameterTypes().length == 1 && (method.getReturnType().equals(Void.TYPE) || Packet.class.isAssignableFrom(method.getReturnType()))) {
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
