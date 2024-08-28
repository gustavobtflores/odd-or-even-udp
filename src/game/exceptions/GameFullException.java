package game.exceptions;

public class GameFullException extends RuntimeException {
  public GameFullException(String message) {
    super(message);
  }
}
