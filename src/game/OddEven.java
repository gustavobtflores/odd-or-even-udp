package game;

import game.exceptions.GameFullException;
import game.exceptions.SideAlreadyChosenException;
import game.network.Broadcaster;
import game.network.Receiver;
import game.states.State;
import game.states.WaitingPlayers;
import player.Player;
import player.PlayerSide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class OddEven {
    HashMap<String, Player> players = new HashMap<>();
    private boolean isOver = false;
    private ArrayList<Integer> availableSides;
    private final ArrayList<Integer> playList = new ArrayList<>();
    private State currentState;
    private final HashMap<Player, Boolean> wantToReplay = new HashMap<>();

    public OddEven(){
        this.currentState = new WaitingPlayers(this);
        setAvailableSides();
    }

    private void setAvailableSides() {
        this.availableSides = new ArrayList<>(Arrays.asList(PlayerSide.ODD.ordinal(), PlayerSide.EVEN.ordinal()));
    }

    private void resetPlayList() {
        this.playList.clear();
    }

    private void resetWantToReplay() {
        this.wantToReplay.clear();
    }

    public void changeState(State state) {
        this.currentState = state;
    }

    public void processRequest(Receiver receiver, Broadcaster broadcaster) {
        this.currentState.handle(receiver, broadcaster);
    }

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
        } else {
            throw new SideAlreadyChosenException("Esse lado já foi escolhido por outro jogador");
        }
    }

    public void play(int play) {
        this.playList.add(play);
    }

    public void replay(String playerKey, boolean replay) {
        Player player = players.get(playerKey);

        wantToReplay.put(player, replay);
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

    public boolean hasAllPlayersAnsweredReplay() {
        return this.wantToReplay.size() == 2;
    }

    public boolean allWantToReplay() {
        boolean allReplay = true;

        for(boolean replay: wantToReplay.values()){
            if (!replay) {
                allReplay = false;
                break;
            }
        }

        return allReplay;
    }

    public void restart() {
        setAvailableSides();
        resetPlayList();
        resetWantToReplay();
    }

    public void end(){
        this.isOver = true;
    }

    public HashMap<String, Player> getPlayers() {
        return players;
    }
}
