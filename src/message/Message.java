package message;

import java.io.Serializable;

public class Message implements Serializable {
    private int[] fields;
    public static final int MSG_SIZE = 3;

    public Message(int[] fields) {
        this.fields = fields;
    }

    public int[] getFields() {
        return this.fields;
    }


}
