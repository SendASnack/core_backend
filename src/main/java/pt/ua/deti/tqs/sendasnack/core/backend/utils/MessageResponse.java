package pt.ua.deti.tqs.sendasnack.core.backend.utils;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@Data
@Generated
@NoArgsConstructor
public class MessageResponse {

    private Date timestamp;
    private String message;
    private Object args;

    public MessageResponse(String message) {
        this.timestamp = Date.from(Instant.now());
        this.message = message;
        this.args = null;
    }

    public MessageResponse(String message, Object args) {
        this.timestamp = Date.from(Instant.now());
        this.message = message;
        this.args = args;
    }

}
