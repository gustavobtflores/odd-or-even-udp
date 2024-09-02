import game.GameState;
import game.OddEven;
import game.exceptions.GameFullException;
import game.exceptions.SideAlreadyChosenException;
import message.Message;
import message.MessageFabric;
import player.Player;
import player.PlayerSide;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class UDPServer {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(Config.SERVER_PORT);
        Receiver serverReceiver = new Receiver(socket);
        Broadcaster serverBroadcaster = new Broadcaster(socket);
        System.out.println("Server UDP rodando na porta: " + Config.SERVER_PORT);

        OddEven oddEven = new OddEven();

        while (!oddEven.isOver()) {
            ClientPacket packet = serverReceiver.readMessage();

            if (packet != null) {
                InetAddress originAddress = packet.address();
                int originPort = packet.port();

                try {
                    switch (oddEven.getState()) {
                        case GameState.WAITING_PLAYERS:
                            try {
                                System.out.println("Esperando jogadores se conectarem...");
                                System.out.println("Novo jogador com IP: " + originAddress + " e porta: " + originPort);
                                oddEven.addPlayer(new Player(originAddress, originPort));
                                serverBroadcaster.sendMessage(new ClientPacket(originAddress, originPort, MessageFabric.createOkMessage()));

                                if (oddEven.isFull()) {
                                    oddEven.setState(GameState.WAITING_PLAYERS_CHOOSE_SIDE);

                                    for (Player player : oddEven.getPlayers().values()) {
                                        Message msgState = MessageFabric.createGameStateMessage(GameState.WAITING_PLAYERS_CHOOSE_SIDE);
                                        serverBroadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), msgState));
                                    }
                                }
                            } catch (GameFullException e) {
                                Message errorMsg = MessageFabric.createErrorMessage();
                                serverBroadcaster.sendMessage(new ClientPacket(originAddress, originPort, errorMsg));
                            }

                            break;
                        case WAITING_PLAYERS_CHOOSE_SIDE:
                            System.out.println("Esperando jogadores escolherem seus lados...");

                            if (packet.message().isChooseSideMessage()) {
                                PlayerSide chosenSide = PlayerSide.values()[packet.message().getFields()[1]];
                                System.out.println("Escolha de lado recebida do IP: " + originAddress + " e porta: " + originPort + " valor: " + chosenSide);

                                String playerKey = originAddress.toString() + originPort;

                                try {
                                    oddEven.chooseSide(playerKey, chosenSide);
                                    serverBroadcaster.sendMessage(new ClientPacket(originAddress, originPort, MessageFabric.createOkMessage()));
                                } catch (SideAlreadyChosenException e) {
                                    serverBroadcaster.sendMessage(new ClientPacket(originAddress, originPort, MessageFabric.createErrorMessage()));
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Thread.sleep(1000);
        }
    }

    private static class Broadcaster extends Thread {
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

                        System.out.println("Enviando mensagem para o IP: " + destinationIp + " e porta: " + destinationPort);

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

    public static class Receiver extends Thread {
        private final DatagramSocket socket;
        private final Queue<ClientPacket> queue;

        public Receiver(DatagramSocket socket) {
            this.socket = socket;
            this.queue = new ArrayBlockingQueue<>(10);

            this.start();
        }

        public synchronized ClientPacket readMessage() {
            return this.queue.poll();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];

                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    this.socket.receive(receivePacket);

                    ByteArrayInputStream byteInStream = new ByteArrayInputStream(buffer);
                    ObjectInputStream objectInStream = new ObjectInputStream(byteInStream);
                    Message msg = (Message) objectInStream.readObject();

                    this.queue.offer(new ClientPacket(receivePacket.getAddress(), receivePacket.getPort(), msg));
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public record ClientPacket(InetAddress address, int port, Message message) {}
}