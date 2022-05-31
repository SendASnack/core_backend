package pt.ua.deti.tqs.sendasnack.core.backend.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.RIDER);
    }

    @AfterEach
    void tearDown() {
        testEntityManager.clear();
    }

    @Test
    @DisplayName("Find user by username")
    void findByUsername() {

        userRepository.save(user);

        User loadedUser = userRepository.findByUsername(user.getUsername());

        assertThat(loadedUser)
                .isNotNull()
                .isEqualTo(user);

    }

    @Test
    @DisplayName("Find non-existent user by username")
    void findNonExistentUserByUsername() {
        User loadedUser = userRepository.findByUsername("UserNotInDB");
        assertThat(loadedUser).isNull();
    }

    @Test
    @DisplayName("Find user by email")
    void findByEmail() {

        userRepository.save(user);

        User loadedUser = userRepository.findByEmail(user.getEmail());

        assertThat(loadedUser)
                .isNotNull()
                .isEqualTo(user);

    }

    @Test
    @DisplayName("Find non-existent user by email")
    void findNonExistentUserByEmail() {
        User loadedUser = userRepository.findByEmail("UserNotInDB@ua.pt");
        assertThat(loadedUser).isNull();
    }

    @Test
    @DisplayName("Check if user exists by username")
    void existsByUsername() {

        userRepository.save(user);
        assertThat(userRepository.existsByUsername(user.getUsername())).isTrue();

    }

    @Test
    @DisplayName("Check non-existent user by username")
    void noExistsByUsername() {
        assertThat(userRepository.existsByUsername("UserNotInDB")).isFalse();
    }

    @Test
    @DisplayName("Check if user exists by email")
    void existsByEmail() {

        userRepository.save(user);
        assertThat(userRepository.existsByEmail(user.getEmail())).isTrue();

    }

    @Test
    @DisplayName("Check non-existent user by email")
    void noExistsByEmail() {
        assertThat(userRepository.existsByEmail("UserNotInDB@ua.pt")).isFalse();
    }

}