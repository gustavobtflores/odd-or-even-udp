package game.states;

import game.GameStateEnum;
import game.OddEven;
import game.network.Broadcaster;
import game.network.Receiver;
import game.utils.ClientPacket;
import message.MessageFabric;
import player.Player;

public class ComputeResult extends State {
    String message = "Calculando o resultado da partida...";

    public ComputeResult(OddEven game) {
        super(game);
        logStateMessage(message);
    }

    @Override
    public void handle(Receiver receiver, Broadcaster broadcaster) {
        Player winnerPlayer = game.computeWinner();

        if (winnerPlayer != null) {
            for (Player player : game.getPlayers().values()) {
                GameStateEnum winStateEnum = player.equals(winnerPlayer) ? GameStateEnum.PLAYER_WIN : GameStateEnum.PLAYER_LOSE;
                broadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), MessageFabric.createEndGameMessage(winStateEnum)));
            }

            game.changeState(new WaitingPlayersRestartOrEnd(game));
        }
    }
}
