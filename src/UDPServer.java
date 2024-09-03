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
        ClientPacket packet = null;

        GameState lastLoggedGameState = null;

        while (!oddEven.isOver()) {
            if(lastLoggedGameState != oddEven.getState()){
                logGameState(oddEven.getState());
                lastLoggedGameState = oddEven.getState();
            }

            try {
                switch (oddEven.getState()) {
                    case GameState.WAITING_PLAYERS:
                        packet = serverReceiver.readMessage();
                        if(packet == null) break;

                        try {
                            oddEven.addPlayer(new Player(packet.address(), packet.port()));
                            System.out.println("Novo jogador com IP: " + packet.address() + " e porta: " + packet.port());
                            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));

                            if (oddEven.isFull()) {
                                oddEven.setState(GameState.WAITING_PLAYERS_CHOOSE_SIDE);

                                for (Player player : oddEven.getPlayers().values()) {
                                    Message msgState = MessageFabric.createGameStateMessage(GameState.WAITING_PLAYERS_CHOOSE_SIDE);
                                    serverBroadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), msgState));
                                }
                            }
                        } catch (GameFullException e) {
                            Message errorMsg = MessageFabric.createErrorMessage();
                            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), errorMsg));
                        }

                        break;
                    case WAITING_PLAYERS_CHOOSE_SIDE:
                        packet = serverReceiver.readMessage();

                        if(packet == null || !packet.message.isChooseSideMessage()) break;

                        PlayerSide chosenSide = PlayerSide.values()[packet.message().getFields()[1]];
                        System.out.println("Escolha de lado recebida do IP: " + packet.address() + " e porta: " + packet.port() + " valor: " + chosenSide);

                        String playerKey = packet.address().toString() + packet.port();

                        try {
                            oddEven.chooseSide(playerKey, chosenSide);
                            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));

                            if (oddEven.hasAllPlayersChosenSide()) {
                                oddEven.setState(GameState.WAITING_PLAYERS_CHOOSE_PLAY);

                                for (Player player : oddEven.getPlayers().values()) {
                                    Message msgState = MessageFabric.createGameStateMessage(GameState.WAITING_PLAYERS_CHOOSE_PLAY);
                                    serverBroadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), msgState));
                                }
                            }

                        } catch (SideAlreadyChosenException e) {
                            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createErrorMessage()));
                        }
                        break;
                    case WAITING_PLAYERS_CHOOSE_PLAY:
                        packet = serverReceiver.readMessage();
                        if(packet == null) break;

                        if (packet.message().isPlayMessage()) {
                            int playerPlay = packet.message().getFields()[1];
                            oddEven.play(playerPlay);
                            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));
                        }

                        if (oddEven.hasAllPlayersPlayed()) oddEven.setState(GameState.COMPUTE_RESULT);

                        System.out.println(oddEven.hasAllPlayersPlayed());

                        break;

                    case COMPUTE_RESULT:
                        Player winnerPlayer = oddEven.computeWinner();

                        if (winnerPlayer != null) {
                            for (Player player : oddEven.getPlayers().values()) {
                                serverBroadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), MessageFabric.createEndGameMessage(player.equals(winnerPlayer))));
                            }
                        }

                        break;
                    case WAITING_PLAYERS_RESTART_OR_END:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(1000);
        }
    }

    public static void logGameState(GameState state) {
        switch(state) {
            case WAITING_PLAYERS:
                System.out.println("Esperando jogadores se conectarem...");
                break;
            case WAITING_PLAYERS_CHOOSE_SIDE:
                System.out.println("Esperando jogadores escolherem seus lados...");
                break;
            case WAITING_PLAYERS_CHOOSE_PLAY:
                System.out.println("Esperando jogadores escolherem suas jogadas...");
                break;
            case COMPUTE_RESULT:
                System.out.println("Calculando resultado do jogo...");
            case WAITING_PLAYERS_RESTART_OR_END:
                System.out.println("Aguardando jogadores decidirem se desejam jogar ou finalizar o jogo");
            default:
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

    public record ClientPacket(InetAddress address, int port, Message message) {
    }
}