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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.UserDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.model.Delivery;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.OrderRequestRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.UserRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.LoginResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.services.DeliveryService;
import pt.ua.deti.tqs.sendasnack.core.backend.services.UserService;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
class RiderControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private UserService userService;

    private Delivery delivery;

    private UserDAO userDAO;

    private HttpHeaders httpHeaders;

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
    void setUp() throws IOException {

        userDAO = new UserDAO("Hugo1307", "hugo@ua.pt", "123456", "Hugo", "91", AccountRoleEnum.RIDER);

        restTemplate.postForEntity("/api/auth/register", userDAO, MessageResponse.class);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity("/api/auth/login", new LoginRequest(userDAO.getEmail(), userDAO.getPassword()), LoginResponse.class);

        httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(response.getBody().getToken());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        OrderRequest orderRequest = new ObjectMapper().readValue("{\n" +
                "    \"businessUsername\": \"Hugo1307\",\n" +
                "    \"costumer\": {\n" +
                "        \"name\": \"Costumer\",\n" +
                "        \"email\": \"hugo@ua.pt\",\n" +
                "        \"address\": {\n" +
                "            \"city\": \"Aveiro\",\n" +
                "            \"street\": \"Rua do Sol\",\n" +
                "            \"postalCode\": \"5680-654\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"order\": {\n" +
                "        \"date\": \"2022-05-30 00:00:00\",\n" +
                "        \"totalPrice\": 25.00,\n" +
                "        \"products\": [\n" +
                "            {\n" +
                "                \"name\": \"Product 1\",\n" +
                "                \"description\": \"This is the new product\",\n" +
                "                \"ingredients\": [\n" +
                "                    \"Lettice\",\n" +
                "                    \"Tomato\"\n" +
                "                ],\n" +
                "                \"price\": 25.00\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"deliveryTime\": \"2022-05-31 01:00:00\",\n" +
                "    \"orderStatus\": \"READY\"\n" +
                "}", OrderRequest.class);

        orderRequest.setOrder(null);
        orderRequest.setCostumer(null);

        orderRequestRepository.save(orderRequest);

        delivery = new Delivery(1L, orderRequest, Date.from(Instant.now()), DeliveryStatus.READY, null);

    }

    @AfterEach
    void tearDown() {
        // deliveryRepository.deleteAll();
    }

    @Test
    void acceptDelivery() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange(String.format("/api/rider/deliveries/%s/accept", 0), HttpMethod.PATCH, new HttpEntity<>(delivery, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("Unable to find the delivery %s.", 0));

        Long deliveryID = deliveryService.save(delivery);

        response = restTemplate.exchange(String.format("/api/rider/deliveries/%s/accept", deliveryID), HttpMethod.PATCH, new HttpEntity<>(delivery, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("Delivery successfully accepted.");

    }

    @Test
    void rejectDelivery() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange(String.format("/api/rider/deliveries/%s/reject", 0), HttpMethod.PATCH, new HttpEntity<>(delivery, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("Unable to find the delivery %s.", 0));

        Long deliveryID = deliveryService.save(delivery);

        assertThat(((RiderUser) userService.findByUsername(userDAO.getUsername())).getRejectedDeliveries()).isEmpty();

        response = restTemplate.exchange(String.format("/api/rider/deliveries/%s/reject", deliveryID), HttpMethod.PATCH, new HttpEntity<>(delivery, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("Delivery successfully rejected.");

        assertThat(((RiderUser) userService.findByUsername("Hugo1307")).getRejectedDeliveries()).isNotEmpty().hasSize(1);

    }

    @Test
    void changeDeliveryStatus() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange(String.format("/api/rider/deliveries/%s/status", 0), HttpMethod.PATCH, new HttpEntity<>(DeliveryStatus.DELIVERED, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("Unable to find a delivery with id %s.", 0));

        assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);

        Long deliveryID = deliveryService.save(delivery);

        response = restTemplate.exchange(String.format("/api/rider/deliveries/%s/status", deliveryID), HttpMethod.PATCH, new HttpEntity<>(DeliveryStatus.DELIVERED, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("Delivery status changed to %s.", DeliveryStatus.DELIVERED));

        assertThat(deliveryService.getDeliveryById(1L)).isNotNull().isPresent();
        assertThat(deliveryService.getDeliveryById(1L).get().getDeliveryStatus()).isNotNull().isEqualTo(DeliveryStatus.DELIVERED);

    }

    @Test
    void getRiderDeliveries() {

        ResponseEntity<Delivery[]> response;

        deliveryService.save(delivery);

        response = restTemplate.exchange("/api/rider/deliveries", HttpMethod.GET, new HttpEntity<>(httpHeaders), Delivery[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();

        response = restTemplate.exchange(String.format("/api/rider/deliveries?deliveryFilter=%s", DeliveryFilter.ONGOING), HttpMethod.GET, new HttpEntity<>(httpHeaders), Delivery[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();

        response = restTemplate.exchange(String.format("/api/rider/deliveries?deliveryFilter=%s", DeliveryFilter.HISTORY_ACCEPTED), HttpMethod.GET, new HttpEntity<>(httpHeaders), Delivery[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();

        response = restTemplate.exchange(String.format("/api/rider/deliveries?deliveryFilter=%s", DeliveryFilter.HISTORY_REJECTED), HttpMethod.GET, new HttpEntity<>(httpHeaders), Delivery[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();

    }

    @Test
    void getUserProfile() {

        ResponseEntity<RiderUser> response;

        response = restTemplate.exchange(String.format("/api/rider/profile/%s", userDAO.getUsername()), HttpMethod.GET, new HttpEntity<>(httpHeaders), RiderUser.class);

        assertThat(response.getBody()).isNotNull().isEqualTo(userRepository.findByUsername(userDAO.getUsername()));

    }

    @Test
    void changeAvailabilityStatus() {

        ResponseEntity<MessageResponse> response;

        response = restTemplate.exchange(String.format("/api/rider/profile/%s/availability", userDAO.getUsername()), HttpMethod.PATCH, new HttpEntity<>(AvailabilityStatus.ONLINE, httpHeaders), MessageResponse.class);

        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("The status was changed to %s.", AvailabilityStatus.ONLINE));

        assertThat(userService.findByUsername(userDAO.getUsername())).isNotNull();
        assertThat((RiderUser) userService.findByUsername(userDAO.getUsername())).extracting(RiderUser::getAvailabilityStatus).isEqualTo(AvailabilityStatus.ONLINE);

    }

}