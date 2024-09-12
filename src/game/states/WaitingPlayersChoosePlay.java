package game.states;

import game.OddEven;
import game.network.Broadcaster;
import game.network.Receiver;
import game.utils.ClientPacket;
import message.MessageFabric;

public class WaitingPlayersChoosePlay extends State {
    String message = "Aguardando jogadores escolherem suas jogadas...";

    public WaitingPlayersChoosePlay(OddEven game) {
        super(game);
        logStateMessage(message);
    }

    @Override
    public void handle(Receiver receiver, Broadcaster broadcaster) {
        ClientPacket packet = receiver.readMessage();

        if (!packet.message().isPlayMessage()) return;

        Object playerPlay = packet.message().getValue();
        String playerKey = packet.address().toString() + packet.port();

        game.play(playerKey, (Integer) playerPlay);
        broadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));

        if (game.hasAllPlayersPlayed()) game.changeState(new ComputeResult(game));
    }
}
