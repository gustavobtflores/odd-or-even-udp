package game.network;

import game.utils.ClientPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Broadcaster extends Thread {
    private final DatagramSocket socket;
    private final Queue<ClientPacket> queue;

    public Broadcaster(DatagramSocket socket) {
        this.socket = socket;
        this.queue = new ArrayBlockingQueue<>(10);
        this.start();
    }

    public void sendMessage(ClientPacket packet) {
        this.queue.offer(packet);
    }

    @Override
    public void run() {
        while (true) {
            if (!queue.isEmpty()) {
                try {
                    ClientPacket clientPacket = queue.remove();

                    InetAddress destinationIp = clientPacket.address();
                    int destinationPort = clientPacket.port();

                    ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream);
                    objectOutStream.writeObject(clientPacket.message());
                    byte[] objectData = byteOutStream.toByteArray();

                    DatagramPacket packet = new DatagramPacket(objectData, objectData.length, destinationIp, destinationPort);

                    this.socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
