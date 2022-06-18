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
import pt.ua.deti.tqs.sendasnack.core.backend.utils.AccountRoleEnum;
import pt.ua.deti.tqs.sendasnack.core.backend.dao.UserDAO;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.OrderStatus;
import pt.ua.deti.tqs.sendasnack.core.backend.repository.OrderRequestRepository;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.LoginRequest;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.MessageResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.security.auth.AuthTokenResponse;
import pt.ua.deti.tqs.sendasnack.core.backend.services.OrderRequestService;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
class BusinessControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    @Autowired
    private OrderRequestService orderRequestService;

    private OrderRequest orderRequest;

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
    public void setUp() throws IOException {

        UserDAO user = new UserDAO("Hugo1307", "hugo@ua.pt", "123456", "Hugo", "91", AccountRoleEnum.RIDER);

        restTemplate.postForEntity("/api/auth/register", user, MessageResponse.class);
        ResponseEntity<AuthTokenResponse> response = restTemplate.postForEntity("/api/auth/login", new LoginRequest(user.getEmail(), user.getPassword()), AuthTokenResponse.class);

        httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(response.getBody().getToken());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        orderRequest = new ObjectMapper().readValue("{\n" +
                "    \"id\": 1,\n" +
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
                "    \"orderStatus\": \"PREPARING\"\n" +
                "}", OrderRequest.class);

    }

    @AfterEach
    public void tearDown() {
        orderRequestRepository.deleteAll();
    }

    @Test
    void createOrder() {

        assertThat(orderRequestService.getAllOrders()).isNotNull().isEmpty();

        orderRequest.setId(null);

        ResponseEntity<MessageResponse> response = restTemplate.postForEntity("/api/business/orders", new HttpEntity<>(orderRequest, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("Your order was successfully placed.");

        assertThat(orderRequestService.getAllOrders()).isNotNull().hasSize(1).doesNotContainNull();

    }

    @Test
    void cancelOrder() {

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);

        ResponseEntity<MessageResponse> response = restTemplate.exchange(String.format("/api/business/orders/%s", orderRequest.getId()), HttpMethod.DELETE, new HttpEntity<>(orderRequest, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("The order %s was cancelled.", orderRequest.getId()));

        assertThat(orderRequestService.getAllOrders()).isNotNull().isEmpty();

    }

    @Test
    void cancelOrderWithIllegalStatus() {

        orderRequest.setOrderStatus(OrderStatus.READY);

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);

        ResponseEntity<MessageResponse> response = restTemplate.exchange(String.format("/api/business/orders/%s", orderRequest.getId()), HttpMethod.DELETE, new HttpEntity<>(orderRequest, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("The order cannot be canceled cause it is no longer cancellable.");

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);

    }

    @Test
    void cancelOrderWith3rdPartyBusiness() {

        orderRequest.setBusinessUsername("OtherBusiness");

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);

        ResponseEntity<MessageResponse> response = restTemplate.exchange(String.format("/api/business/orders/%s", orderRequest.getId()), HttpMethod.DELETE, new HttpEntity<>(null, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("You don't have permission to cancel that order.");

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);

    }

    @Test
    void changeOrderStatus() {

        orderRequest.setOrderStatus(OrderStatus.PREPARING);

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();
        assertThat(orderRequestService.getById(orderRequest.getId()).get().getOrderStatus()).isNotNull().isEqualTo(OrderStatus.PREPARING);

        ResponseEntity<MessageResponse> response = restTemplate.exchange(String.format("/api/business/orders/%s/status", orderRequest.getId()), HttpMethod.PATCH, new HttpEntity<>(OrderStatus.READY, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo(String.format("The status of the order %s was changed to %s.", orderRequest.getId(), OrderStatus.READY));

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();
        assertThat(orderRequestService.getById(orderRequest.getId()).get().getOrderStatus()).isNotNull().isEqualTo(OrderStatus.READY);

    }

    @Test
    void changeOrderStatusWith3rdPartyBusiness() {

        orderRequest.setBusinessUsername("OtherBusiness");
        orderRequest.setOrderStatus(OrderStatus.PREPARING);

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();

        ResponseEntity<MessageResponse> response = restTemplate.exchange(String.format("/api/business/orders/%s/status", orderRequest.getId()), HttpMethod.PATCH, new HttpEntity<>(OrderStatus.READY, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("You don't have permission to edit that order.");

    }

    @Test
    void getOrderStatus() {

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();

        ResponseEntity<String> response = restTemplate.exchange(String.format("/api/business/orders/%s", orderRequest.getId()), HttpMethod.GET, new HttpEntity<>(null, httpHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("\"" + orderRequest.getOrderStatus().toString() + "\"");

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();
        assertThat(orderRequestService.getById(orderRequest.getId()).get().getOrderStatus()).isNotNull().isEqualTo(orderRequest.getOrderStatus());

    }

    @Test
    void getOrderStatusWith3rdPartyBusiness() {

        orderRequest.setBusinessUsername("OtherBusiness");

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();

        ResponseEntity<MessageResponse> response = restTemplate.exchange(String.format("/api/business/orders/%s", orderRequest.getId()), HttpMethod.GET, new HttpEntity<>(null, httpHeaders), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).extracting(MessageResponse::getMessage).isEqualTo("You don't have permission to view that order.");

    }

    @Test
    void getAllOrders() {

        ResponseEntity<OrderRequest[]> response;

        response = restTemplate.exchange("/api/business/orders", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), OrderRequest[].class);

        assertThat(response.getBody()).isNotNull().isEmpty();

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();

        response = restTemplate.exchange("/api/business/orders", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), OrderRequest[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    void getAllOrdersWith3rdPartyBusiness() {

        orderRequest.setBusinessUsername("OtherBusiness");

        ResponseEntity<OrderRequest[]> response;

        response = restTemplate.exchange("/api/business/orders", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), OrderRequest[].class);

        assertThat(response.getBody()).isNotNull().isEmpty();

        Long orderRequestId = orderRequestService.save(orderRequest);
        orderRequest.setId(orderRequestId);

        assertThat(orderRequestService.getAllOrders()).isNotNull().isNotEmpty().doesNotContainNull().hasSize(1);
        assertThat(orderRequestService.getById(orderRequest.getId())).isNotNull().isNotEmpty();

        response = restTemplate.exchange("/api/business/orders", HttpMethod.GET, new HttpEntity<>(null, httpHeaders), OrderRequest[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();

    }

}