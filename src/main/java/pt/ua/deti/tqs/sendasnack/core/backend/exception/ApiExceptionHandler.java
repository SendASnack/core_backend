package pt.ua.deti.tqs.sendasnack.core.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.*;

import java.util.Date;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequestException(BadRequestException badRequestException, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), badRequestException.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userNotFoundException(UserNotFoundException userNotFoundException, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), userNotFoundException.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> orderNotFoundException(OrderNotFoundException orderNotFoundException, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), orderNotFoundException.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalOrderStatusException.class)
    public ResponseEntity<?> illegalOrderStatusException(IllegalOrderStatusException illegalOrderStatusException, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), illegalOrderStatusException.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<?> forbiddenOperationException(ForbiddenOperationException forbiddenOperationException, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(new Date(), forbiddenOperationException.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

}