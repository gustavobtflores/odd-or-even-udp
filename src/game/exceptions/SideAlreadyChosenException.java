package game.exceptions;

public class SideAlreadyChosenException extends RuntimeException {
    public SideAlreadyChosenException(String message) {
        super(message);
    }
}
