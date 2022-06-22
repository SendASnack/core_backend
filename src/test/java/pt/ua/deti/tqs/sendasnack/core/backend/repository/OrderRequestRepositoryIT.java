package pt.ua.deti.tqs.sendasnack.core.backend.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.model.OrderRequest;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderRequestRepositoryIT {

    @Autowired
    private OrderRequestRepository orderRequestRepository;

    private OrderRequest orderRequest;
    private OrderRequest orderRequestDiffBusiness;

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
        registry.add("spring.datasource.url", mariaDb::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDb::getUsername);
        registry.add("spring.datasource.password", mariaDb::getPassword);
    }

    @BeforeEach
    void setUp() throws IOException {
        orderRequest = new ObjectMapper().readValue("{\n" +
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

        orderRequestDiffBusiness = new ObjectMapper().readValue("{\n" +
                "    \"businessUsername\": \"Hugo\",\n" +
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

    }

    @AfterEach
    void tearDown() {
        orderRequestRepository.deleteAll();
    }

    @Test
    void findById() {

        assertDoesNotThrow(() -> orderRequestRepository.save(orderRequest));

        assertThat(orderRequestRepository.findById(orderRequest.getId())).isNotNull().isPresent();

        OrderRequest orderRequestFound = orderRequestRepository.findById(orderRequest.getId()).get();

        assertThat(orderRequestFound.getId()).isEqualTo(orderRequest.getId());
        assertThat(orderRequestFound.getBusinessUsername()).isEqualTo(orderRequest.getBusinessUsername());
        assertThat(orderRequestFound.getCostumer()).isEqualTo(orderRequest.getCostumer());
        assertThat(orderRequestFound.getOrderStatus()).isEqualTo(orderRequest.getOrderStatus());
        assertThat(orderRequestFound.getDeliveryTime().compareTo(orderRequest.getDeliveryTime()) == 0).isTrue();

        assertThat(orderRequestRepository.findById(6000L)).isEmpty();

    }

    @Test
    void findAllByBusinessUsername() {

        assertDoesNotThrow(() -> orderRequestRepository.save(orderRequest));
        assertDoesNotThrow(() -> orderRequestRepository.save(orderRequestDiffBusiness));

        assertThat(orderRequestRepository.findAllByBusinessUsername("Hugo1307")).isNotNull().hasSize(1);
        assertThat(orderRequestRepository.findAllByBusinessUsername("Hugo")).isNotNull().hasSize(1);
        assertThat(orderRequestRepository.findAllByBusinessUsername("NonExistentBusiness")).isNotNull().isEmpty();

    }
}