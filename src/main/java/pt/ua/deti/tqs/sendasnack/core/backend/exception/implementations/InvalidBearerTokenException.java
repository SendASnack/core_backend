package pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations;

public class InvalidBearerTokenException extends RuntimeException {

    public InvalidBearerTokenException(String message) {
        super(message);
    }

}
