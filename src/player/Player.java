package player;

import java.net.InetAddress;

public class Player {
    private final InetAddress address;
    private final int port;

    public Player(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
