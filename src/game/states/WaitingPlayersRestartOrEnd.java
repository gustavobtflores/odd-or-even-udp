package game.states;

import game.OddEven;
import game.network.Broadcaster;
import game.network.Receiver;
import game.utils.ClientPacket;
import message.MessageFabric;

public class WaitingPlayersRestartOrEnd extends State {
    String message = "Aguardando jogadores decidirem se desejam continuar jogando ou encerrar o jogo";

    public WaitingPlayersRestartOrEnd(OddEven game) {
        super(game);
        logStateMessage(message);
    }

    @Override
    public void handle(Receiver receiver, Broadcaster broadcaster) {
        ClientPacket packet = receiver.readMessage();
        if(packet == null) return;
    }
}
