package player;

import java.net.InetAddress;

public class Player {
    private final InetAddress address;
    private final int port;
    private PlayerSide side;

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

    public void setSide(PlayerSide side) {
        this.side = side;
    }

    public PlayerSide getSide() {
        return side;
    }
}
