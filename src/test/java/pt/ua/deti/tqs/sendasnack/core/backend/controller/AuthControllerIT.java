package pt.ua.deti.tqs.sendasnack.core.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.UserDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.ErrorDetails;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;
import pt.ua.deti.tqs.sendasnack.core.backend.requests.LoginRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.requests.MessageResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthTokenResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.services.UserService;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext
class AuthControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private UserDAO userDAO;

    @Container
    public static MariaDBContainer<?> mariaDb = new MariaDBContainer<>(DockerImageName.parse("mariadb"))
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
    public void setUp() {
        userDAO = new UserDAO("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.RIDER);
        userService.removeAll();
    }

    @AfterEach
    public void tearDown() {
        userService.removeAll();
    }

    @Test
    @DisplayName("Register new user")
    void registerUser() {

        ResponseEntity<MessageResponse> response = restTemplate.postForEntity("/api/auth/register", userDAO, MessageResponse.class);

        List<User> users = userService.getAllUsers();

        assertThat(userService.findByUsername(userDAO.getUsername())).isNotNull();
        assertThat(users).hasSize(1).doesNotContainNull();

        assertThat(users).extracting(User::getUsername).containsOnly(userDAO.getUsername());
        assertThat(users).extracting(User::getEmail).containsOnly(userDAO.getEmail());
        assertThat(users).extracting(User::getName).containsOnly(userDAO.getName());
        assertThat(users).extracting(User::getPhoneNumber).containsOnly(userDAO.getPhoneNumber());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("The user was successfully registered!");

    }

    @Test
    @DisplayName("Login with correct credentials")
    void loginUser() {

        restTemplate.postForEntity("/api/auth/register", userDAO, MessageResponse.class);

        LoginRequest loginRequest = new LoginRequest(userDAO.getEmail(), userDAO.getPassword());

        ResponseEntity<AuthTokenResponse> response = restTemplate.postForEntity("/api/auth/login", loginRequest, AuthTokenResponse.class);
        AuthTokenResponse authTokenResponse = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(authTokenResponse).isNotNull();
        assertThat(authTokenResponse).extracting(AuthTokenResponse::getToken).isNotNull();
        assertThat(authTokenResponse).extracting(AuthTokenResponse::getMessage).isEqualTo("Authentication succeeded.");

    }

    @Test
    @DisplayName("Login with wrong credentials")
    void loginWithWrongCredentials() {

        restTemplate.postForEntity("/api/auth/register", userDAO, MessageResponse.class);

        LoginRequest loginRequest = new LoginRequest(userDAO.getEmail(), "wrong_password");

        ResponseEntity<ErrorDetails> response = restTemplate.postForEntity("/api/auth/login", loginRequest, ErrorDetails.class);
        ErrorDetails errorDetailsResponse = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(errorDetailsResponse).isNotNull();
        assertThat(errorDetailsResponse).extracting(ErrorDetails::getTimestamp).isNotNull();
        assertThat(errorDetailsResponse).extracting(ErrorDetails::getMessage).isEqualTo("The provided password is wrong.");

    }

    @Test
    @DisplayName("Login with wrong body request")
    void loginWithWrongBodyRequest() {

        LoginRequest loginRequest = new LoginRequest(null, null);

        ResponseEntity<ErrorDetails> response = restTemplate.postForEntity("/api/auth/login", loginRequest, ErrorDetails.class);
        ErrorDetails errorDetailsResponse = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorDetailsResponse).isNotNull();
        assertThat(errorDetailsResponse).extracting(ErrorDetails::getTimestamp).isNotNull();
        assertThat(errorDetailsResponse).extracting(ErrorDetails::getMessage).isEqualTo("Please provide a valid request body.");

    }

}