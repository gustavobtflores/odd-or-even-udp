package game;

import game.exceptions.GameFullException;
import game.exceptions.SideAlreadyChosenException;
import player.Player;
import player.PlayerSide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class OddEven {
    HashMap<String, Player> players = new HashMap<>();
    private boolean isOver = false;
    private GameState state = GameState.WAITING_PLAYERS;
    private ArrayList<Integer> availableSides = new ArrayList<>(Arrays.asList(PlayerSide.ODD.ordinal(), PlayerSide.EVEN.ordinal()));
    private ArrayList<Integer> playList = new ArrayList<>();

    public OddEven(){}

    public void addPlayer(Player player) throws GameFullException {
        if(isFull()) {
            throw new GameFullException("O jogo já atingiu o máximo de jogadores");
        }

        String playerKey = player.getAddress().toString() + player.getPort();

        this.players.put(playerKey, player);
    }

    public void chooseSide(String playerKey, PlayerSide side) throws SideAlreadyChosenException {
        if(availableSides.contains(side.ordinal())) {
            availableSides.remove((Integer) side.ordinal());

            Player player = players.get(playerKey);

            player.setSide(side);
            System.out.println("Lado " + side + " escolhido pelo jogador " + player.getAddress() + " " + player.getPort());
        } else {
            throw new SideAlreadyChosenException("Esse lado já foi escolhido por outro jogador");
        }
    }

    public void play(int play) {
        this.playList.add(play);
    }

    public Player computeWinner(){
        int playsSum = 0;

        for (int play: playList){
            playsSum += play;
        }

        PlayerSide winnerSide = playsSum % 2 == 0 ? PlayerSide.EVEN : PlayerSide.ODD;

        for(Player player: players.values()){
            if(player.getSide() == winnerSide) {
                return player;
            }
        }

        return null;
    }

    public boolean isFull(){
        return this.players.size() == 2;
    }

    public boolean isOver() {
        return isOver;
    }

    public boolean hasAllPlayersChosenSide(){
        return this.availableSides.isEmpty();
    }

    public boolean hasAllPlayersPlayed(){
        return this.playList.size() == 2;
    }

    public GameState getState(){
        return this.state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }
}
