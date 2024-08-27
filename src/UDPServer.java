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

        DatagramPacket received = server.receive();
        String message = new String(received.getData()).trim();

        server.send(message+" ACK", received.getAddress(), received.getPort());

        server.close();
    }
}