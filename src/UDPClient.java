import game.GameStateEnum;
import message.Message;
import message.MessageFabric;
import player.PlayerSide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        Connection connection = new Connection(socket, InetAddress.getByName(Config.SERVER_ADDRESS), Config.SERVER_PORT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        GameStateEnum gameStateEnum = GameStateEnum.PLAYER_CONNECTING_SERVER;

        int counter = 0;
        Message msg = null;

        gameLoop: while (gameStateEnum != GameStateEnum.ENDED) {
            switch (gameStateEnum) {
                case GameStateEnum.PLAYER_CONNECTING_SERVER:
                    connection.sendMessage(MessageFabric.createConnectionMessage());
                    msg = connection.readMessage(2000);

                    System.out.println(msg);

                    if(msg != null && !msg.isErrorMessage()) {
                        gameStateEnum = GameStateEnum.PLAYER_WAITING_OTHERS;
                    } else {
                        System.out.println("O servidor já está lotado");
                        System.out.println("Desconectando...");
                        break gameLoop;
                    }

                    Thread.sleep(500);

                    break;
                case PLAYER_WAITING_OTHERS:
                    msg = connection.readMessage();

                    if (msg != null && msg.isGameStateMessage() && msg.getFields()[1] == GameStateEnum.WAITING_PLAYERS_CHOOSE_SIDE.ordinal()) {
                        gameStateEnum = GameStateEnum.PLAYER_CHOOSING_SIDE;
                        System.out.println("Jogadores conectados");
                    } else if (counter <= 0) {
                        System.out.println("Esperando jogadores se conectarem...");
                        counter++;
                    }

                    break;
                case PLAYER_CHOOSING_SIDE:
                    System.out.println("Escolha o lado que deseja:\n1 - Ímpar\n2 - Par");

                    int playerChoose = Integer.parseInt(reader.readLine());

                    connection.sendMessage(MessageFabric.createChooseSideMessage(PlayerSide.values()[playerChoose - 1]));

                    msg = connection.readMessage(2000);

                    if(msg != null){
                        if(msg.isErrorMessage()){
                            System.out.println("Esse lado já foi escolhido por outro jogador!");
                            continue;
                        } else {
                            gameStateEnum = GameStateEnum.PLAYER_WAITING_OPPONENT_CHOOSE;
                        }
                    }

                    break;
                case PLAYER_WAITING_OPPONENT_CHOOSE:
                    System.out.println("Aguarde o outro jogador escolher o lado...");

                    while(true) {
                        msg = connection.readMessage(2000);

                        if(msg != null && msg.getFields()[1] == GameStateEnum.WAITING_PLAYERS_CHOOSE_PLAY.ordinal()) {
                            gameStateEnum = GameStateEnum.PLAYER_CHOOSING_PLAY;
                            break;
                        } else {
                            Thread.sleep(500);
                        }
                    }

                    break;
                case PLAYER_CHOOSING_PLAY:
                    System.out.println("Digite o número que deseja jogar: ");
                    int playerPlay = Integer.parseInt(reader.readLine());
                    connection.sendMessage(MessageFabric.createPlayMessage(playerPlay));

                    msg = connection.readMessage(2000);

                    if(msg != null) {
                        gameStateEnum = GameStateEnum.PLAYER_WAITING_RESULT;
                    }

                    break;
                case PLAYER_WAITING_RESULT:
                    msg = connection.readMessage(2000);

                    if(msg != null && msg.isEndGameMessage()) {
                        String endGameMessage = msg.getFields()[1] != 0 ? "Você ganhou :)" : "Você perdeu :(";
                        System.out.println(endGameMessage);
                        gameStateEnum = GameStateEnum.PLAYER_RESTART_OR_END;
                    }

                    break;
                case PLAYER_RESTART_OR_END:
                    System.out.println("Você deseja continuar jogando?\n1 - Sim\n2 - Não");
                    reader.readLine();

                    break;
                default:
            }

            Thread.sleep(500);
        }

        connection.stop();
        socket.close();
        System.exit(0);
    }
}