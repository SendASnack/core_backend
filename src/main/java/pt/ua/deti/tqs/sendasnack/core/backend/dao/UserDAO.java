package pt.ua.deti.tqs.sendasnack.core.backend.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.BusinessUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AccountRoleEnum;

import java.util.HashSet;

@Generated
@Data
@AllArgsConstructor
public class UserDAO implements IEntityDAO<User> {

    private String username;
    private String email;
    private String password;
    private String name;
    private String phoneNumber;
    private AccountRoleEnum accountType;

    @Override
    public User toDataEntity() {

        if (accountType == AccountRoleEnum.BUSINESS) {
            return new BusinessUser(username, email, password, name, phoneNumber);
        } else {
            return new RiderUser(username, email, password, name, phoneNumber, new HashSet<>(), new HashSet<>());
        }

    }

}
