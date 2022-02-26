package com.focess.core.net;

import com.focess.Main;
import com.focess.api.exceptions.IllegalPortException;
import com.focess.api.net.PacketPreCodec;
import com.focess.api.net.Receiver;
import com.focess.api.net.Socket;
import com.focess.api.net.packet.ConnectPacket;
import com.focess.api.net.packet.Packet;
import com.focess.api.net.packet.SidedConnectPacket;
import com.focess.api.util.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

public class FocessUDPSocket implements Socket {

    private final Map<Class<? extends Packet>, List<Pair<Receiver,Method>>> packetMethods = Maps.newHashMap();
    private final List<Receiver> receivers = Lists.newArrayList();
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private final Thread thread;

    public FocessUDPSocket(int port) throws IllegalPortException {
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new IllegalPortException(port);
        }
        this.packet = new DatagramPacket(new byte[1024*1024],1024*1024);
        this.thread = new Thread(()->{
            Main.getLogger().debugLang("start-focess-udp-socket",port);
            while (true) {
                try {
                    socket.receive(this.packet);
                    PacketPreCodec packetPreCodec = new PacketPreCodec();
                    packetPreCodec.push(this.packet.getData(),this.packet.getOffset(),this.packet.getLength());
                    Packet packet = packetPreCodec.readPacket();
                    if (packet != null) {
                        if (packet instanceof SidedConnectPacket) {
                            String name = ((SidedConnectPacket) packet).getName();
                            packet = new ConnectPacket(this.packet.getAddress().getHostName(),this.packet.getPort(),name);
                        }
                        for (Pair<Receiver, Method> pair : packetMethods.getOrDefault(packet.getClass(), Lists.newArrayList())) {
                            Method method = pair.getValue();
                            try {
                                method.setAccessible(true);
                                Object o = method.invoke(pair.getKey(), packet);
                                if (o != null) {
                                    PacketPreCodec handler = new PacketPreCodec();
                                    handler.writePacket((Packet)o);
                                    DatagramPacket sendPacket = new DatagramPacket(handler.getBytes(),handler.getBytes().length,this.packet.getSocketAddress());
                                    socket.send(sendPacket);
                                }
                            } catch (Exception e) {
                                Main.getLogger().thrLang("exception-handle-packet", e);
                            }
                        }
                    }
                } catch (IOException e) {
                    Main.getLogger().thrLang("exception-focess-udp-socket",e);
                }
            }
        });
        this.thread.start();
    }

    @Override
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

    @Override
    public void close() {
        this.socket.close();
        this.thread.stop();
        for (Receiver receiver: receivers)
            receiver.close();
    }

    public void sendPacket(String host, int port, Packet packet) {
        PacketPreCodec handler = new PacketPreCodec();
        handler.writePacket(packet);
        DatagramPacket sendPacket = new DatagramPacket(handler.getBytes(),handler.getBytes().length,new InetSocketAddress(host,port));
        try {
            this.socket.send(sendPacket);
        } catch (IOException e) {
            Main.getLogger().thrLang("exception-send-packet",e);
        }
    }
}
