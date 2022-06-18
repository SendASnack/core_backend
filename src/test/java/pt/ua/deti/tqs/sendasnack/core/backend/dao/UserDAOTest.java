package pt.ua.deti.tqs.sendasnack.core.backend.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.BusinessUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AccountRoleEnum;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Test
    void toDataEntity() {

        UserDAO riderUserDAO = new UserDAO("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.RIDER);
        UserDAO businessUserDAO = new UserDAO("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.BUSINESS);

        User riderUser = new RiderUser(riderUserDAO.getUsername(), riderUserDAO.getEmail(), riderUserDAO.getPassword(), riderUserDAO.getName(), riderUserDAO.getPhoneNumber(), new HashSet<>(), new HashSet<>());
        User businessUser = new BusinessUser(businessUserDAO.getUsername(), businessUserDAO.getEmail(), businessUserDAO.getPassword(), businessUserDAO.getName(), businessUserDAO.getPhoneNumber());

        assertThat(riderUserDAO.toDataEntity()).isEqualTo(riderUser);
        assertThat(businessUserDAO.toDataEntity()).isEqualTo(businessUser);

    }

}