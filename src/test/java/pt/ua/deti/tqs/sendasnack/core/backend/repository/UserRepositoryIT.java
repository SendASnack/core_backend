package pt.ua.deti.tqs.sendasnack.core.backend.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.User;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @Container
    public static final MariaDBContainer<?> mariaDb = new MariaDBContainer<>(DockerImageName.parse("mariadb"))
            .withDatabaseName("SendASnack_Core_Test")
            .withUsername("admin")
            .withPassword("admin")
            .withExposedPorts(3306)
            .waitingFor(new HttpWaitStrategy().forPort(3306)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",mariaDb::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDb::getUsername);
        registry.add("spring.datasource.password", mariaDb::getPassword);

    }

    @BeforeEach
    void setUp() {
        user = new User("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
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