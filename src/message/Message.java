package message;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType messageType;
    private final Object value;

    public Message(MessageType messageType, Object value) {
        this.messageType = messageType;
        this.value = value;
    }

    public Message(MessageType messageType) {
        this.messageType = messageType;
        this.value = null;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Object getValue() {
        return value;
    }

    public boolean isConnectionMessage() {
        return this.messageType == MessageType.CONNECT;
    }

    public boolean isChooseSideMessage(){
        return this.messageType == MessageType.CHOOSE_SIDE;
    }

    public boolean isGameStateMessage() {
        return this.messageType == MessageType.GAME_STATE;
    }

    public boolean isErrorMessage() {
        return this.messageType == MessageType.ERROR;
    }

    public boolean isPlayMessage() {
        return this.messageType == MessageType.PLAY;
    }

    public boolean isEndGameMessage() {
        return this.messageType == MessageType.END_GAME;
    }

    public boolean isRestartGameMessage() {
        return this.messageType == MessageType.RESTART_GAME;
    }

    public boolean isOkMessage() {
        return this.messageType == MessageType.OK;
    }
}
