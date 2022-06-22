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
import org.testcontainers.utility.DockerImageName;
import pt.ua.deti.tqs.sendasnack.core.backend.model.*;
import pt.ua.deti.tqs.sendasnack.core.backend.model.users.RiderUser;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.DeliveryStatus;
import pt.ua.deti.tqs.sendasnack.core.backend.utils.OrderStatus;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DeliveryRepositoryIT {

    @Autowired
    private DeliveryRepository deliveryRepository;

    private Delivery deliveryWithoutRider;

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
        OrderRequest orderRequest = new OrderRequest(null, "", new Costumer(null, "", "", new Address()), new Order(), Date.from(Instant.now()), OrderStatus.READY);
        deliveryWithoutRider = new Delivery(2L, orderRequest, Date.from(Instant.now()), DeliveryStatus.READY, null);
    }

    @AfterEach
    void tearDown() {
        deliveryRepository.deleteAll();
    }

    @Test
    void findAllByRiderUsernameIsNull() {

        assertThat(deliveryRepository.findAllByRiderIsNull()).isNotNull().isEmpty();

        deliveryRepository.save(deliveryWithoutRider);

        assertThat(deliveryRepository.findAllByRiderIsNull()).isNotNull().hasSize(1);
        assertThat(deliveryRepository.findAllByRiderIsNull()).isNotNull().extracting(Delivery::getRider).containsOnly((RiderUser) null);

    }

    @Test
    void findById() {

        assertThat(deliveryRepository.findById(1L)).isNotNull().isEmpty();

        deliveryRepository.save(deliveryWithoutRider);

        assertThat(deliveryRepository.findById(1L)).isNotNull().isNotEmpty();
        assertThat(deliveryRepository.findById(1L)).get().extracting(Delivery::getId).isEqualTo(1L);
        assertThat(deliveryRepository.findById(1L)).get().extracting(Delivery::getDeliveryStatus).isEqualTo(DeliveryStatus.READY);
        assertThat(deliveryRepository.findById(1L)).get().extracting(Delivery::getRider).isEqualTo(null);


    }

}