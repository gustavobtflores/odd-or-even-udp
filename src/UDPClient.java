import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

    private DatagramSocket socket;
    private InetAddress address;
    private int port;

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

        Thread sendThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String message;
                while ((message = reader.readLine()) != null) {
                    client.send(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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

        sendThread.start();
        receiveThread.start();

        client.send("Connection test");
    }
}