package message;

import java.io.Serializable;

public class Message implements Serializable {
    private final int[] fields;
    public static final int MSG_SIZE = 2;

    public Message(int[] fields) {
        this.fields = fields;
    }

    public int[] getFields() {
        return this.fields;
    }

    public boolean isConnectionMessage() {
        return this.fields[0] == MessageType.CONNECT.ordinal();
    }

    public boolean isChooseSideMessage(){
        return this.fields[0] == MessageType.CHOOSE_SIDE.ordinal();
    }

    public boolean isGameStateMessage() {
        return this.fields[0] == MessageType.GAME_STATE.ordinal();
    }

    public boolean isErrorMessage() {
        return this.fields[0] == MessageType.ERROR.ordinal();
    }

    public boolean isPlayMessage() {
        return this.fields[0] == MessageType.PLAY.ordinal();
    }

    public boolean isEndGameMessage() {
        return this.fields[0] == MessageType.END_GAME.ordinal();
    }
}
