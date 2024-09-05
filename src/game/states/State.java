package game.states;

import game.OddEven;
import game.network.Broadcaster;
import game.network.Receiver;

public abstract class State {
    protected OddEven game;

    public State(OddEven game){
        this.game = game;
    }

    public abstract void handle(Receiver receiver, Broadcaster broadcaster);

    public void logStateMessage(String message){
        System.out.println(message);
    }
}
