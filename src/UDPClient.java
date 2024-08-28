import game.GameState;
import player.PlayerMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;

    public UDPClient(String address, int port) throws IOException {
        this.address = InetAddress.getByName(address);
        this.port = port;
        this.socket = new DatagramSocket();
    }

    public void send(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
    }

    public String receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    public static void main(String[] args) throws IOException {
        UDPClient client = new UDPClient(Config.SERVER_ADDRESS, Config.SERVER_PORT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        Thread receiveThread = new Thread(() -> {
            while (true) {
                try {
                    String receivedMessage = client.receive();
                    System.out.println(receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        receiveThread.start();

        GameState gameState = GameState.PLAYER_CONNECTING_SERVER;

        while (gameState != GameState.ENDED) {
            switch(gameState){
                case GameState.PLAYER_CONNECTING_SERVER:
                    client.send(PlayerMessage.CONNECT.name());
                    gameState = GameState.PLAYER_CHOOSING_SIDE;
                case GameState.PLAYER_CHOOSING_SIDE:
                    System.out.println("Qual lado você deseja?\n1 - Par\n2 - Ímpar");
                    String playerChoose = reader.readLine();
                    client.send(playerChoose);
            }
        }
    }
}