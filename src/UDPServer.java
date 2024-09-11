import game.OddEven;
import game.network.Broadcaster;
import game.network.Receiver;

import java.net.DatagramSocket;

public class UDPServer {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(Config.SERVER_PORT);
        Receiver serverReceiver = new Receiver(socket);
        Broadcaster serverBroadcaster = new Broadcaster(socket);
        System.out.println("Server UDP rodando na porta: " + Config.SERVER_PORT);

        OddEven oddEven = new OddEven();

        while (!oddEven.isOver()) {
            try {
                oddEven.processRequest(serverReceiver, serverBroadcaster);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(1000);
        }

        System.out.println("Jogo encerrado, finalizando processo...");
        System.exit(0);
    }
}