package message;

import game.GameStateEnum;
import player.PlayerSide;

public class MessageFabric {
    public static Message createConnectionMessage() {
        return new Message(MessageType.CONNECT);
    }

    public static Message createChooseSideMessage(PlayerSide side) {
        return new Message(MessageType.CHOOSE_SIDE, side);
    }

    public static Message createGameStateMessage(GameStateEnum state) {
        return new Message(MessageType.GAME_STATE, state);
    }

    public static Message createErrorMessage() {
        return new Message(MessageType.ERROR);
    }

    public static Message createOkMessage() {
        return new Message(MessageType.OK);
    }

    public static Message createPlayMessage(int play){
        return new Message(MessageType.PLAY, play);
    }

    public static Message createEndGameMessage(GameStateEnum gameResult) {
        return new Message(MessageType.END_GAME, gameResult);
    }

    public static Message createRestartGameMessage(GameStateEnum restartDecision) {
        return new Message(MessageType.RESTART_GAME, restartDecision);
    }
}
