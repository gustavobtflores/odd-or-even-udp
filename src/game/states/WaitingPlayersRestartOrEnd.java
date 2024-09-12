package game.states;

import game.GameStateEnum;
import game.OddEven;
import game.network.Broadcaster;
import game.network.Receiver;
import game.utils.ClientPacket;
import message.MessageFabric;
import player.Player;

public class WaitingPlayersRestartOrEnd extends State {
    String message = "Aguardando jogadores decidirem se desejam continuar jogando ou encerrar o jogo";

    public WaitingPlayersRestartOrEnd(OddEven game) {
        super(game);
        logStateMessage(message);
    }

    @Override
    public void handle(Receiver receiver, Broadcaster broadcaster) {
        ClientPacket packet = receiver.readMessage();

        if (!packet.message().isRestartGameMessage()) return;

        broadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));

        String playerKey = packet.address().toString() + packet.port();

        game.replay(playerKey, packet.message().getValue() == GameStateEnum.PLAYER_RESTART);

        if (game.hasAllPlayersAnsweredReplay()) {
            if (game.allWantToReplay()) {
                for (Player player : game.getPlayers().values()) {
                    broadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), MessageFabric.createRestartGameMessage(GameStateEnum.PLAYER_RESTART)));
                }

                game.restart();
                game.changeState(new WaitingPlayersChooseSide(game));
            } else {
                for (Player player : game.getPlayers().values()) {
                    broadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), MessageFabric.createRestartGameMessage(GameStateEnum.PLAYER_END)));
                }

                game.end();
            }
        }
    }
}
