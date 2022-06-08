package pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations;

public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }

}
