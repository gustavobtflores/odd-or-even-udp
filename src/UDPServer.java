import game.GameState;
import game.OddEven;
import player.Player;
import player.PlayerMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class UDPServer {
    private int port = Config.SERVER_PORT;
    private final DatagramSocket socket;
    private byte[] buffer = new byte[1024];

    public UDPServer(int port) throws SocketException {
        this.port = port;
        this.socket = new DatagramSocket(port);
    }

    public UDPServer() throws SocketException {
        this.socket = new DatagramSocket(this.port);
    }

    private DatagramPacket receive() throws IOException {
        Arrays.fill(buffer, (byte) 0);
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(receivedPacket);

        return receivedPacket;
    }

    private void send(String message, InetAddress address, int port) throws IOException {
        byte[] messageBytes = message.getBytes();
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length , address, port);
        socket.send(packet);
    }

    public void close() {
        this.socket.close();
    }

    public static void main(String[] args) throws Exception {
        UDPServer server = new UDPServer();
        System.out.println("UDP server running on port "+server.port);

        OddEven oddEven = new OddEven();

        while(!oddEven.isOver()) {
            DatagramPacket received = server.receive();
            String message = new String(received.getData()).trim();

            InetAddress ip = received.getAddress();
            int port = received.getPort();

            try {
                switch(oddEven.getState()){
                    case GameState.WAITING_PLAYERS:
                        System.out.println("Waiting players connection...");
                        System.out.println("New player with IP: "+ip+" and port: "+port);
                        oddEven.addPlayer(new Player(ip, port));
                        if(oddEven.isFull()){
                            oddEven.setState(GameState.WAITING_PLAYERS_CHOOSE_SIDE);
                        }
                        break;
                    case WAITING_PLAYERS_CHOOSE_SIDE:
                        System.out.println("Waiting players to choose sides...");
                        break;
                }
            } catch (Exception e) {
                server.send(e.getMessage(), ip, port);
            }
        }

        server.close();
    }
}