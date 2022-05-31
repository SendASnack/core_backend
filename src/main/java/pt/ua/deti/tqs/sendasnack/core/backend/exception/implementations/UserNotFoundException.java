package pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String message) {
        super(message);
    }

}
