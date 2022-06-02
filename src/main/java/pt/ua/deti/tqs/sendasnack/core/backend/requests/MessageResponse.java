package pt.ua.deti.tqs.sendasnack.core.backend.requests;

import lombok.Data;
import lombok.Generated;

import java.util.Date;

@Data
@Generated
public class MessageResponse {
    private final Date timestamp;
    private final String message;
}
