package pt.ua.deti.tqs.sendasnack.core.backend.security.auth;

import lombok.Data;
import lombok.Generated;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;

@Data
@Generated
public class LoginResponse {

    private final String message;
    private final String token;
    private final User user;

}
