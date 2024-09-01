import message.Message;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Connection {
    private final Receiver input;
    private final Broadcaster output;
    private final InetAddress ip;
    private final int port;

    public Connection(DatagramSocket socket, InetAddress ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;

        this.output = new Broadcaster(socket);
        this.input = new Receiver(socket);

        this.output.start();
        this.input.start();
    }

    public synchronized Message readMessage() {
        return this.input.readMessage();
    }

    public synchronized Message readMessage(long timeoutMs) throws InterruptedException{

        Message msg = null;
        long ini = System.currentTimeMillis();

        while(((msg = this.input.readMessage()) == null) && (System.currentTimeMillis() - ini < timeoutMs)) {
            Thread.sleep(50);
        }
        return msg;
    }

    public synchronized void sendMessage(Message message) {
        this.output.sendMessage(message);
    }


    private class Receiver extends Thread {
        private final DatagramSocket socket;
        private final Queue<Message> queue;

        public Receiver(DatagramSocket socket) {
            this.socket = socket;
            this.queue = new ArrayBlockingQueue<>(10);
        }

        public Message readMessage() {
            return this.queue.poll();
        }

        public void clearInput() {
            this.queue.clear();
        }

        @Override
        public void run() {
            while (true) {

                try {
                    byte[] buffer = new byte[1024];

                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    this.socket.receive(receivePacket);

                    ByteArrayInputStream byteInStream = new ByteArrayInputStream(buffer);
                    ObjectInputStream objectInStream = new ObjectInputStream(byteInStream);
                    Message msg = (Message) objectInStream.readObject();

                    this.queue.offer(msg);

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Broadcaster extends Thread {
        private final DatagramSocket socket;
        private final Queue<Message> queue;

        public Broadcaster(DatagramSocket socket) {
            this.socket = socket;
            this.queue = new ArrayBlockingQueue<>(10);
        }

        public void sendMessage(Message message) {
            this.queue.offer(message);
        }

        @Override
        public void run() {
            while (true) {
                if (!queue.isEmpty()) {
                    try {
                        Message msg = queue.remove();

                        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
                        ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream);
                        objectOutStream.writeObject(msg);
                        byte[] objectData = byteOutStream.toByteArray();

                        DatagramPacket packet = new DatagramPacket(objectData, objectData.length, ip, port);

                        System.out.println("Enviando mensagem para o IP: " + ip + " e porta: " + port);

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
}
