package message;

import game.GameState;
import player.PlayerSide;

public class MessageFabric {
    public static Message createConnectionMessage() {
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.CONNECT.ordinal();

        return msg;
    }

    public static Message createChooseSideMessage(PlayerSide side) {
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.CHOOSE_SIDE.ordinal();
        msg.getFields()[1] = side.ordinal();

        return msg;
    }

    public static Message createGameStateMessage(GameState state) {
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.GAME_STATE.ordinal();
        msg.getFields()[1] = state.ordinal();

        return msg;
    }

    public static Message createErrorMessage() {
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.ERROR.ordinal();

        return msg;
    }

    public static Message createOkMessage() {
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.OK.ordinal();

        return msg;
    }

    public static Message createPlayMessage(int play){
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.PLAY.ordinal();
        msg.getFields()[1] = play;

        return msg;
    }

    public static Message createEndGameMessage(boolean winner) {
        Message msg = new Message(new int[Message.MSG_SIZE]);

        msg.getFields()[0] = MessageType.END_GAME.ordinal();
        msg.getFields()[1] = winner ? 1 : 0;

        return msg;
    }
}
