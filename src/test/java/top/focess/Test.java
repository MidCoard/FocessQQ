package top.focess;

import com.focess.api.net.*;
import com.focess.api.net.packet.MessagePacket;
import com.focess.core.net.FocessSidedClientReceiver;
import com.focess.core.net.FocessSidedClientSocket;
import com.focess.core.net.FocessSidedReceiver;
import com.focess.core.net.FocessSidedSocket;
import lombok.SneakyThrows;

public class Test {

    @SneakyThrows
    public static void main(String[] args) {
        FocessSidedSocket focessSocket = new FocessSidedSocket(1234);
        ServerReceiver server;
        focessSocket.registerReceiver(server = new FocessSidedReceiver());
        server.registerPackHandler("CLIENT0", MessagePacket.class, packet -> {
            System.out.println(packet.getMessage());
        });
        FocessSidedClientSocket clientSocket = new FocessSidedClientSocket("127.0.0.1",1234);
        FocessSidedClientReceiver client;
        clientSocket.registerReceiver(client = new FocessSidedClientReceiver(clientSocket,"CLIENT0"));
        client.registerPackHandler(MessagePacket.class,packet -> {
            System.out.println("Client: " + packet.getMessage());
        });
        while(!client.isConnected());
        client.sendPacket(new MessagePacket("Hello"));
        server.sendPacket("CLIENT0",new MessagePacket("FUCK"));
        server.sendPacket("CLIENT0",new MessagePacket("FUCK"));
        server.sendPacket("CLIENT0",new MessagePacket("FUCK"));
        server.sendPacket("CLIENT0",new MessagePacket("FUCK"));
        server.sendPacket("CLIENT0",new MessagePacket("FUCK"));
        server.sendPacket("CLIENT0",new MessagePacket("FUCK"));
    }
}
