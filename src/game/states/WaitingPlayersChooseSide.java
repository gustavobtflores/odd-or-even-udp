package game.states;

import game.GameStateEnum;
import game.OddEven;
import game.exceptions.SideAlreadyChosenException;
import game.network.Broadcaster;
import game.network.Receiver;
import game.utils.ClientPacket;
import message.Message;
import message.MessageFabric;
import player.Player;
import player.PlayerSide;

public class WaitingPlayersChooseSide extends State {
    String message = "Aguardando jogadores escolherem seus lados...";

    public WaitingPlayersChooseSide(OddEven game) {
        super(game);
        logStateMessage(message);
    }

    @Override
    public void handle(Receiver receiver, Broadcaster broadcaster) {
        ClientPacket packet = receiver.readMessage();

        if(packet == null || !packet.message().isChooseSideMessage()) return;

        PlayerSide chosenSide = PlayerSide.values()[packet.message().getFields()[1]];
        System.out.println("Escolha de lado recebida do IP: " + packet.address() + " e porta: " + packet.port() + " valor: " + chosenSide);

        String playerKey = packet.address().toString() + packet.port();

        try {
            game.chooseSide(playerKey, chosenSide);
            broadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createOkMessage()));

            if (game.hasAllPlayersChosenSide()) {
                game.changeState(new WaitingPlayersChoosePlay(game));

                for (Player player : game.getPlayers().values()) {
                    Message msgState = MessageFabric.createGameStateMessage(GameStateEnum.WAITING_PLAYERS_CHOOSE_PLAY);
                    broadcaster.sendMessage(new ClientPacket(player.getAddress(), player.getPort(), msgState));
                }
            }

        } catch (SideAlreadyChosenException e) {
            broadcaster.sendMessage(new ClientPacket(packet.address(), packet.port(), MessageFabric.createErrorMessage()));
        }
    }
}
