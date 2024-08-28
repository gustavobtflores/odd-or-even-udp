package game;

public enum GameState {
    //Server
    WAITING_PLAYERS,
    WAITING_PLAYERS_CHOOSE_SIDE,


    //Client
    PLAYER_CONNECTING_SERVER,
    PLAYER_CHOOSING_SIDE,

    //Both
    ENDED,
}
