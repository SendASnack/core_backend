package pt.ua.deti.tqs.sendasnack.core.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.UserDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.ErrorDetails;
import pt.ua.deti.tqs.sendasnack.core.backend.model.User;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.UserRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.requests.LoginRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.requests.MessageResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthTokenResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO("Hugo1307", "hugogoncalves13@ua.pt", "12345", "Hugo", "910", AccountRoleEnum.RIDER);
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Register new user")
    void registerUser() {

        ResponseEntity<MessageResponse> response = restTemplate.postForEntity("/api/auth/register", userDAO, MessageResponse.class);

        List<User> users = userRepository.findAll();

        assertThat(userRepository.findByUsername(userDAO.getUsername())).isNotNull();
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