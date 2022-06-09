package pt.ua.deti.tqs.sendasnack.core.backend.requests;

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

    public MessageResponse(String message) {
        this.timestamp = Date.from(Instant.now());
        this.message = message;
    }

}
