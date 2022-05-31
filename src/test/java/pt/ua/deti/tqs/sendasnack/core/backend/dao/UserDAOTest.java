package pt.ua.deti.tqs.sendasnack.core.backend.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Test
    void toDataEntity() {

        UserDAO userDAO = new UserDAO("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.RIDER);
        User user = new User(userDAO.getUsername(), userDAO.getEmail(), userDAO.getPassword(), userDAO.getName(), userDAO.getPhoneNumber(), userDAO.getAccountType());

        assertThat(userDAO.toDataEntity()).isEqualTo(user);

    }

}