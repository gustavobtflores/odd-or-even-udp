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

        GameStateEnum gameState = GameStateEnum.PLAYER_CONNECTING_SERVER;

        int counter = 0;
        Message msg;

        gameLoop: while (gameState != GameStateEnum.ENDED) {
            switch (gameState) {
                case GameStateEnum.PLAYER_CONNECTING_SERVER:
                    System.out.println("Conectando-se ao servidor...");
                    connection.sendMessage(MessageFabric.createConnectionMessage());
                    msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                    if(msg != null) {
                        if(!msg.isErrorMessage()){
                            System.out.println("Conectado!");
                            gameState = GameStateEnum.PLAYER_WAITING_OTHERS;
                        } else {
                            System.out.println("O servidor já está lotado");
                            System.out.println("Desconectando...");
                            break gameLoop;
                        }
                    }

                    break;
                case PLAYER_WAITING_OTHERS:
                    msg = connection.readMessage();

                    if (msg != null && msg.isGameStateMessage() && msg.getValue() == GameStateEnum.WAITING_PLAYERS_CHOOSE_SIDE) {
                        gameState = GameStateEnum.PLAYER_CHOOSING_SIDE;
                        System.out.println("Jogadores conectados");
                    } else if (counter <= 0) {
                        System.out.println("Esperando outros jogadores se conectarem...");
                        counter++;
                    }

                    break;
                case PLAYER_CHOOSING_SIDE:
                    System.out.println("Escolha o lado que deseja:\n1 - Ímpar\n2 - Par");

                    try {
                        int playerChoose = Integer.parseInt(reader.readLine());

                        connection.sendMessage(MessageFabric.createChooseSideMessage(PlayerSide.values()[playerChoose - 1]));

                        msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                        if(msg != null){
                            if(msg.isErrorMessage()){
                                System.out.println("Esse lado já foi escolhido por outro jogador, tente novamente!");
                                continue;
                            } else {
                                gameState = GameStateEnum.PLAYER_WAITING_OPPONENT_CHOOSE;
                            }
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        System.out.println("Valor de lado inválido, tente novamente!");
                    }

                    break;
                case PLAYER_WAITING_OPPONENT_CHOOSE:
                    System.out.println("Aguarde o outro jogador escolher o lado...");

                    while(true) {
                        msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                        if(msg != null && msg.getValue() == GameStateEnum.WAITING_PLAYERS_CHOOSE_PLAY) {
                            gameState = GameStateEnum.PLAYER_CHOOSING_PLAY;
                            break;
                        } else {
                            Thread.sleep(500);
                        }
                    }

                    break;
                case PLAYER_CHOOSING_PLAY:
                    try {
                        System.out.println("Digite o número que deseja jogar: ");
                        int playerPlay = Integer.parseInt(reader.readLine());
                        connection.sendMessage(MessageFabric.createPlayMessage(playerPlay));

                        msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                        if(msg != null) {
                            gameState = GameStateEnum.PLAYER_WAITING_RESULT;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Valor de lado inválido, tente novamente!");
                    }

                    break;
                case PLAYER_WAITING_RESULT:
                    msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                    if(msg != null && msg.isEndGameMessage()) {
                        String endGameMessage = msg.getValue() == GameStateEnum.PLAYER_WIN ? "Você ganhou :)" : "Você perdeu :(";
                        System.out.println(endGameMessage);
                        gameState = GameStateEnum.PLAYER_RESTART_OR_END;
                    }

                    break;
                case PLAYER_RESTART_OR_END:
                    try {
                        System.out.println("Você deseja continuar jogando?\n1 - Sim\n2 - Não");
                        int playerRestart = Integer.parseInt(reader.readLine());
                        connection.sendMessage(MessageFabric.createRestartGameMessage(playerRestart == 1 ? GameStateEnum.PLAYER_RESTART : GameStateEnum.PLAYER_END));

                        if(playerRestart != 1) {
                            gameState = GameStateEnum.ENDED;
                            System.out.println("Jogo encerrado, desconectando...");

                            while(connection.readMessage(Config.RESPONSE_TIMEOUT) == null) continue;

                            break;
                        }

                        msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                        if(msg != null){
                            if(msg.isOkMessage()) {
                                gameState = GameStateEnum.PLAYER_WAITING_OPPONENT_RESTART;
                            } else if(msg.isRestartGameMessage() && msg.getValue() != GameStateEnum.PLAYER_RESTART) {
                                gameState = GameStateEnum.ENDED;
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Opção inválida, tente novamente!");
                    }
                    break;
                case PLAYER_WAITING_OPPONENT_RESTART:
                    msg = connection.readMessage(Config.RESPONSE_TIMEOUT);

                    if(msg != null && msg.isRestartGameMessage()){
                        if(msg.getValue() == GameStateEnum.PLAYER_RESTART) {
                            gameState = GameStateEnum.PLAYER_CHOOSING_SIDE;
                        } else {
                            System.out.println("O outro jogador decidiu não continuar jogando, desconectando...");
                            gameState = GameStateEnum.ENDED;
                            break;
                        }
                    }

                    System.out.println("Aguardando outro jogador decidir se deseja continuar jogando ou não...");

                    break;
            }
        }

        connection.stop();
        socket.close();
        System.exit(0);
    }
}