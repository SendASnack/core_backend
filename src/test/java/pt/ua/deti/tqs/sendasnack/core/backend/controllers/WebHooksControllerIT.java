package pt.ua.deti.tqs.sendasnack.core.backend.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.UserDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.exception.implementations.AlreadyExistentUserException;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.Hook;
import pt.ua.deti.tqs.sendasnack.core.backend.model.webhooks.WebHook;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.WebHookRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthTokenResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.services.WebHookService;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.LoginRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.MessageResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.WebHookEvent;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
class WebHooksControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebHookRepository webHookRepository;

    @Autowired
    private WebHookService webHookService;

    private WebHook webHook;

    private HttpHeaders riderHttpHeaders;
    private HttpHeaders businessHttpHeaders;

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

        UserDAO riderUserDAO = new UserDAO("Hugo1307", "hugo@ua.pt", "123456", "Hugo", "91", AccountRoleEnum.RIDER);
        UserDAO businessUserDAO = new UserDAO("Hugo1307_B", "hugo_business@ua.pt", "123456", "Hugo", "91", AccountRoleEnum.BUSINESS);

        try {
            restTemplate.postForEntity("/api/auth/register", riderUserDAO, MessageResponse.class);
            restTemplate.postForEntity("/api/auth/register", businessUserDAO, MessageResponse.class);
        } catch (AlreadyExistentUserException e) {
            // Ignored:
            // It should throw many exceptions because we are trying to register users that already exist.
            // We are not interested in those right now, so we ignore it.
        }

        ResponseEntity<AuthTokenResponse> loginResponseRider = restTemplate.postForEntity("/api/auth/login", new LoginRequest(riderUserDAO.getEmail(), riderUserDAO.getPassword()), AuthTokenResponse.class);
        ResponseEntity<AuthTokenResponse> loginResponseBusiness = restTemplate.postForEntity("/api/auth/login", new LoginRequest(businessUserDAO.getEmail(), businessUserDAO.getPassword()), AuthTokenResponse.class);

        riderHttpHeaders = new HttpHeaders();
        riderHttpHeaders.setBearerAuth(loginResponseRider.getBody().getToken());
        riderHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

        businessHttpHeaders = new HttpHeaders();
        businessHttpHeaders.setBearerAuth(loginResponseBusiness.getBody().getToken());
        businessHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

        webHook = new WebHook(null, businessUserDAO.getUsername(), new Hook(null, "https://myservice.com/", HttpMethod.POST, "VALUE"), WebHookEvent.DELIVERY_STATUS);

    }

    @AfterEach
    void tearDown() {
        webHookRepository.deleteAll();
    }

    @Test
    void registerWebHook() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange("/api/business/webhook", HttpMethod.POST, new HttpEntity<>(webHook, riderHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isNotNull().isEqualTo("You must have a business account to do this.");

        response = restTemplate.exchange("/api/business/webhook", HttpMethod.POST, new HttpEntity<>(webHook, businessHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isNotNull().isEqualTo("Your hook was successfully registered.");

        assertThat(webHookService.getRegisteredWebHooks(webHook.getBusinessUsername()))
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

    }

    @Test
    void deleteWebHook() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange(String.format("/api/business/webhook/%s", 1), HttpMethod.DELETE, new HttpEntity<>(webHook, riderHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isNotNull().isEqualTo("You must have a business account to do this.");

        response = restTemplate.exchange(String.format("/api/business/webhook/%s", 1), HttpMethod.DELETE, new HttpEntity<>(webHook, businessHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isNotNull().isEqualTo(String.format("Unable to find webHook with id %s.", 1));

        Long id = webHookService.save(webHook);

        response = restTemplate.exchange(String.format("/api/business/webhook/%s", id), HttpMethod.DELETE, new HttpEntity<>(webHook, businessHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isNotNull().isEqualTo("Your hook was successfully unregistered.");

    }

    @Test
    void getAllWebHooks() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange(String.format("/api/business/webhook/%s", 1), HttpMethod.DELETE, new HttpEntity<>(webHook, riderHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isNotNull().isEqualTo("You must have a business account to do this.");

        webHookService.save(webHook);

        response = restTemplate.exchange(String.format("/api/business/webhook/%s", 1), HttpMethod.DELETE, new HttpEntity<>(webHook, businessHttpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

    }

}