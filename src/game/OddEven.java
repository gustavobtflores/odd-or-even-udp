package game;

import game.exceptions.GameFullException;
import player.Player;
import player.PlayerSide;

import java.util.ArrayList;
import java.util.Arrays;

public class OddEven {
    ArrayList<Player> players = new ArrayList<>();
    private boolean isOver = false;
    private GameState state = GameState.WAITING_PLAYERS;
    private ArrayList<Integer> availableSides = new ArrayList<>(Arrays.asList(PlayerSide.ODD.ordinal(), PlayerSide.EVEN.ordinal()));

    public OddEven(){}

    public void addPlayer(Player player) {
        if(isFull()) {
            throw new GameFullException("O jogo já atingiu o máximo de jogadores");
        }

        this.players.add(player);
    }

    public boolean isFull(){
        return this.players.size() == 2;
    }

    public boolean isOver() {
        return isOver;
    }

    public GameState getState(){
        return this.state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
