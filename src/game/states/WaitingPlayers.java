package game.states;

import game.GameStateEnum;
import game.OddEven;
import game.exceptions.GameFullException;
import game.network.Broadcaster;
import game.network.Receiver;
import game.utils.ClientPacket;
import message.Message;
import message.MessageFabric;
import player.Player;

public class WaitingPlayers extends State {
    String message = "Aguardando jogadores se conectarem...";

    public WaitingPlayers(OddEven game) {
        super(game);
        logStateMessage(message);
    }

    public void handle(Receiver serverReceiver, Broadcaster serverBroadcaster) {
        ClientPacket packet = serverReceiver.readMessage();
        if(packet == null) return;

        try {
            game.addPlayer(new Player(packet.address(), packet.port()));
            System.out.println("Novo jogador com IP: " + packet.address() + " e porta: " + packet.port());
            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));

            if (game.isFull()) {

                game.changeState(new WaitingPlayersChooseSide(game));

                for (Player player : game.getPlayers().values()) {
                    Message msgState = MessageFabric.createGameStateMessage(GameStateEnum.WAITING_PLAYERS_CHOOSE_SIDE);
                    serverBroadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), msgState));
                }
            }
        } catch (GameFullException e) {
            Message errorMsg = MessageFabric.createErrorMessage();
            serverBroadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), errorMsg));
        }

    }
}
