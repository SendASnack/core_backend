package pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations;

public class AlreadyExistentUserException extends RuntimeException {

    public AlreadyExistentUserException(String message) {
        super(message);
    }

}
