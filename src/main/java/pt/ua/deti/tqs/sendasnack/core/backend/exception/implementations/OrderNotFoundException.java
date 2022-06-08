package pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

}
