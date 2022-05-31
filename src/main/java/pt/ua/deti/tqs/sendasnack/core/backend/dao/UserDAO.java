package pt.ua.deti.tqs.sendasnack.core.backend.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;

@Data
@AllArgsConstructor
@Generated
public class UserDAO implements IEntityDAO<User> {

    private String username;
    private String email;
    private String password;
    private String name;
    private String phoneNumber;
    private AccountRoleEnum accountType;

    @Override
    public User toDataEntity() {
        return new User(username, email, password, name, phoneNumber, accountType);
    }

}
